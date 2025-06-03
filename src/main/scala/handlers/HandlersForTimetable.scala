import com.sun.net.httpserver.HttpExchange
import cats.effect.IO

import Models._
import SQLiteDatabase._
import HttpUtils._ 

object HandlersForTimetable {

  // Показ расписания
  def timetableHandler(exchange: HttpExchange): IO[Unit] = IO.defer {
    val query = exchange.getRequestURI.getQuery
    val params = parseQuery(query)
    val groupOpt = params.get("group").map(_.trim).filter(_.nonEmpty)
    val dayOpt = params.get("day").map(_.trim).filter(_.nonEmpty).map(_.toLowerCase)

    val (header, filteredEntries) = groupOpt match {
      case Some(group) => SQLiteDatabase.getTimetableByGroup(group) match {
        case scala.util.Success(entries) =>
          val filtered = dayOpt match {
            case Some(day) => entries.filter(e => e.day.toLowerCase == day)
            case None => entries
          }
          ("Расписание группы " + group, filtered)
        case scala.util.Failure(_) =>
          ("Ошибка загрузки расписания", Nil)
      }
      case None =>
        ("Введите группу для поиска", Nil)
    }

    for {
      template <- readResourceFile("schedule/schsearch.html")
      timetableListHtml = filteredEntries.map { e =>
        s"<li>${escapeHtml(e.day)}, ${escapeHtml(e.time)} — ${escapeHtml(e.subject)} в ${escapeHtml(e.room)}</li>"
      }.mkString("\n")
      pageHtml = template
        .replace("{group}", groupOpt.getOrElse(""))
        .replace("{header}", header)
        .replace("{timetableList}", timetableListHtml)
      _ <- sendResponse(exchange, pageHtml)
    } yield ()
  }.handleErrorWith { e =>
    sendResponse(exchange, s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>")
  }


  // Подменю действий с расписанием
  def timetableActionsHandler(exchange: HttpExchange): IO[Unit] = {
    readResourceFile("schedule/schfunc.html").flatMap(sendResponse(exchange, _))
    .handleErrorWith(e => sendResponse(exchange, s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"))
  }

  // Добавление записи расписания
  def addHandler(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" => readResourceFile("schedule/schadd.html").flatMap(sendResponse(exchange, _))

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      entry = TimetableEntry(
        params.getOrElse("day", ""),
        params.getOrElse("time", ""),
        params.getOrElse("subject", ""),
        params.getOrElse("room", "")
      )
      group = params.getOrElse("group", "")
      result <- IO(SQLiteDatabase.insertTimetableEntry(entry, group))
      responseHtml = result match {
        case scala.util.Success(_) => "<html><body>Запись добавлена.<br/><a href='/timetableActions'>Назад</a></body></html>"
        case scala.util.Failure(ex) => s"<html><body>Ошибка при добавлении: ${escapeHtml(ex.getMessage)}<br/><a href='/timetableActions'>Назад</a></body></html>"
      }
      _ <- sendResponse(exchange, responseHtml)
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }

  // Удаление записи расписания
  def deleteHandler(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" => for {
      template <- readResourceFile("schedule/schdelete.html")
      entriesTry = SQLiteDatabase.getTimetableByGroup("") // TODO: изменить фильтр, если нужно
      listHtml = entriesTry match {
        case scala.util.Success(entries) =>
          entries.map(e => s"<li>${escapeHtml(e.day)}, ${escapeHtml(e.time)} — ${escapeHtml(e.subject)} в ${escapeHtml(e.room)}</li>").mkString("\n")
        case scala.util.Failure(_) => "<li>Не удалось загрузить расписание</li>"
      }
      pageHtml = template.replace("{timetableList}", listHtml)
      _ <- sendResponse(exchange, pageHtml)
    } yield ()

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      group = params.getOrElse("group", "")
      dayOpt = params.get("day")
      result = dayOpt match {
        case Some(day) => SQLiteDatabase.deleteTimetableByGroupAndDay(group, day)
        case None      => SQLiteDatabase.deleteTimetableByGroup(group)
      }
      responseHtml <- result match {
        case scala.util.Success(deletedCount) if deletedCount > 0 =>
          IO.pure(s"<html><body>Записи удалены.<br/><a href='/timetableActions'>Назад</a></body></html>")
        case scala.util.Success(_) =>
          IO.pure(s"<html><body>Записи не найдены.<br/><a href='/timetableActions/delete'>Назад</a></body></html>")
        case scala.util.Failure(ex) =>
          IO.pure(s"<html><body>Ошибка при удалении: ${escapeHtml(ex.getMessage)}<br/><a href='/timetableActions/delete'>Назад</a></body></html>")
      }
      _ <- sendResponse(exchange, responseHtml)
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }

  // Редактирование записи расписания
  def editHandler(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" => readResourceFile("schedule/schupdate.html").flatMap(sendResponse(exchange, _))

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      searchGroup = params.getOrElse("searchGroup", "").trim
      searchDay = params.getOrElse("searchDay", "").trim

      _ <- if (searchGroup.isEmpty || searchDay.isEmpty) {
        sendResponse(exchange, s"<html><body>Введите группу и день для поиска.<br/><a href='/timetableActions/edit'>Назад</a></body></html>")
      } else {
        SQLiteDatabase.getTimetableByGroup(searchGroup) match {
          case scala.util.Success(entries) => entries.find(e => e.day.equalsIgnoreCase(searchDay)) match {
            case Some(orig) => val updated = TimetableEntry(
              params.getOrElse("day", orig.day),
              params.getOrElse("time", orig.time),
              params.getOrElse("subject", orig.subject),
              params.getOrElse("room", orig.room)
            )

            val deleteRes = SQLiteDatabase.deleteTimetableByGroupAndDay(searchGroup, searchDay)
            val insertRes = SQLiteDatabase.insertTimetableEntry(updated, searchGroup)

            val responseHtml = (deleteRes, insertRes) match {
              case (scala.util.Success(_), scala.util.Success(_)) => s"<html><body>Запись расписания обновлена.<br/><a href='/timetableActions'>Назад</a></body></html>"
              case _ => s"<html><body>Ошибка при обновлении.<br/><a href='/timetableActions/edit'>Назад</a></body></html>"
            }
            sendResponse(exchange, responseHtml)

            case None => sendResponse(exchange, s"<html><body>Запись не найдена.<br/><a href='/timetableActions/edit'>Назад</a></body></html>")
            }
          case scala.util.Failure(_) => sendResponse(exchange, s"<html><body>Ошибка базы данных.<br/><a href='/timetableActions/edit'>Назад</a></body></html>")
        }
      }
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }
}