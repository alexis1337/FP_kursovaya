import com.sun.net.httpserver.HttpExchange
import cats.effect.IO

import Models._
import SQLiteDatabase._
import HttpUtils._  

object HandlersForProfessors {

  // Обработчик просмотра и поиска преподавателей
  def professorsHandler(exchange: HttpExchange): IO[Unit] = {
    val program = for {
      query <- IO(Option(exchange.getRequestURI.getQuery).getOrElse(""))
      params = parseQuery(query)
      searchName = params.get("name").map(_.trim).filter(_.nonEmpty).getOrElse("")

      profsTry <- IO(SQLiteDatabase.getAllProfessors())

      (header, filteredProfs) = if (searchName.isEmpty) ("Все преподаватели", profsTry.getOrElse(Nil))
      else ("Результаты поиска", profsTry.map(_.filter(_.name.equalsIgnoreCase(searchName))).getOrElse(Nil))

      template <- readResourceFile("professors/profsearch.html")

      profListHtml = filteredProfs.map { p =>
        val disciplinesStr = p.disciplines.mkString(", ")
        s"<li>${escapeHtml(p.name)} - ${escapeHtml(p.title)} (${escapeHtml(disciplinesStr)}) [${escapeHtml(p.faculty)}]</li>"
      }.mkString("\n")

      pageHtml = template
        .replace("{searchName}", escapeHtml(searchName))
        .replace("{header}", header)
        .replace("{professorList}", profListHtml)

      _ <- sendResponse(exchange, pageHtml)
    } yield ()

    program.handleErrorWith { e =>
      val errorHtml = s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"
      sendResponse(exchange, errorHtml)
    }
  }

  // Обработчик подменю преподавателей
  def professorActionsHandler(exchange: HttpExchange): IO[Unit] =
    readResourceFile("professors/proffunc.html").flatMap(sendResponse(exchange, _))
      .handleErrorWith(e => sendResponse(exchange, s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"))

  // Обработчик добавления преподавателя
  def addHandlerProfessors(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" =>  readResourceFile("professors/profadd.html").flatMap(sendResponse(exchange, _))
      .handleErrorWith(e => sendResponse(exchange, s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"))

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      prof = Professor(
        params.getOrElse("name", ""),
        params.getOrElse("title", ""),
        params.getOrElse("email", ""),
        params.getOrElse("disciplines", "").split(",").map(_.trim).filter(_.nonEmpty).toList,
        params.getOrElse("faculty", "")
      )
      result <- IO(SQLiteDatabase.insertProfessor(prof))
      responseHtml = result match {
        case scala.util.Success(_) => "<html><body>Преподаватель добавлен.<br/><a href='/professorActions'>Назад</a></body></html>"
        case scala.util.Failure(ex) => s"<html><body>Ошибка при добавлении: ${escapeHtml(ex.getMessage)}<br/><a href='/professorActions'>Назад</a></body></html>"
      }
      _ <- sendResponse(exchange, responseHtml)
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }

  // Обработчик удаления преподавателя
  def deleteHandlerProfessors(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" => for {
      template <- readResourceFile("professors/profdelete.html")
      profsTry <- IO(SQLiteDatabase.getAllProfessors())
      profListHtml = profsTry match {
        case scala.util.Success(profs) =>
          profs.map(p => s"<li>${escapeHtml(p.name)} - ${escapeHtml(p.title)} (${escapeHtml(p.faculty)})</li>").mkString("\n")
        case scala.util.Failure(_) => "<li>Не удалось загрузить список преподавателей</li>"
      }
      pageHtml = template.replace("{professorList}", profListHtml)
      _ <- sendResponse(exchange, pageHtml)
    } yield ()

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      name = params.getOrElse("name", "").trim
      _ <- if (name.isEmpty) {
        sendResponse(exchange, "<html><body>Имя не может быть пустым.<br/><a href='/professorActions/delete'>Назад</a></body></html>")
      } else {
        val result = SQLiteDatabase.deleteProfessor(name)
        val responseHtml = result match {
         case scala.util.Success(deletedCount) if deletedCount > 0 =>
            s"<html><body>Преподаватель '$name' уволен.<br/><a href='/professorActions'>Назад</a></body></html>"
          case scala.util.Success(_) =>
            s"<html><body>Преподаватель '$name' не найден.<br/><a href='/professorActions/delete'>Назад</a></body></html>"
          case scala.util.Failure(ex) =>
            s"<html><body>Ошибка при удалении: ${escapeHtml(ex.getMessage)}<br/><a href='/professorActions/delete'>Назад</a></body></html>"
        }
        sendResponse(exchange, responseHtml)
      }
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }

  // Обработчик редактирования преподавателя
  def editHandlerProfessors(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" => readResourceFile("professors/profupdate.html").flatMap(sendResponse(exchange, _))
      .handleErrorWith(e => sendResponse(exchange, s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"))

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      searchName = params.getOrElse("searchName", "").trim
      _ <- if (searchName.isEmpty) sendResponse(exchange, s"<html><body>Введите имя преподавателя для поиска.<br/><a href='/professorActions/edit'>Назад</a></body></html>")
      else {
        val profsTry = SQLiteDatabase.getAllProfessors()
        profsTry match {
          case scala.util.Success(profs) =>  profs.find(_.name.equalsIgnoreCase(searchName)) match {
            case Some(orig) => val updated = Professor(
              params.get("name").filter(_.nonEmpty).getOrElse(orig.name),
              params.get("title").filter(_.nonEmpty).getOrElse(orig.title),
              params.get("email").filter(_.nonEmpty).getOrElse(orig.email),
              params.get("disciplines").map(_.split(",").map(_.trim).filter(_.nonEmpty).toList).getOrElse(orig.disciplines),
              params.get("faculty").filter(_.nonEmpty).getOrElse(orig.faculty)
            )

            val deleteRes = SQLiteDatabase.deleteProfessor(orig.name)
            val insertRes = SQLiteDatabase.insertProfessor(updated)

            val responseHtml = (deleteRes, insertRes) match {
              case (scala.util.Success(_), scala.util.Success(_)) => s"<html><body>Преподаватель обновлён.<br/><a href='/professorActions'>Назад</a></body></html>"
              case _ => s"<html><body>Ошибка при обновлении.<br/><a href='/professorActions/edit'>Назад</a></body></html>"
            }
            sendResponse(exchange, responseHtml)

            case None => sendResponse(exchange, s"<html><body>Преподаватель '$searchName' не найден.<br/><a href='/professorActions/edit'>Назад</a></body></html>")
            }
          case scala.util.Failure(_) => sendResponse(exchange, s"<html><body>Ошибка базы данных.<br/><a href='/professorActions/edit'>Назад</a></body></html>")
        }
      }
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }
}