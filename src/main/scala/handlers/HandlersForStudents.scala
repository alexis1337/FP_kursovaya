import com.sun.net.httpserver.HttpExchange
import cats.effect.IO

import Models._
import SQLiteDatabase._
import HttpUtils._ 

object HandlersForStudents {

  // Обработчик поиска студентов
  def studentsHandler(exchange: HttpExchange): IO[Unit] = IO.defer {
    val query = exchange.getRequestURI.getQuery
    val params = parseQuery(query)
    val searchName = params.get("name").map(_.trim).filter(_.nonEmpty).getOrElse("")

    val studentsTry = SQLiteDatabase.getAllStudents()

    val (header, filteredStudents) = if (searchName.isEmpty) ("Все студенты", studentsTry.getOrElse(Nil))
    else ("Результаты поиска", studentsTry.map(_.filter(_.name.equalsIgnoreCase(searchName))).getOrElse(Nil))
    
    for {
      template <- readResourceFile("students/studsearch.html")
      studentListHtml = filteredStudents.map(s => s"<li>${escapeHtml(s.name)} - ${escapeHtml(s.group)} (${escapeHtml(s.direction)})</li>").mkString("\n")
      pageHtml = template
        .replace("{searchName}", escapeHtml(searchName))
        .replace("{header}", header)
        .replace("{studentList}", studentListHtml)
      _ <- sendResponse(exchange, pageHtml)
    } yield ()
  }.handleErrorWith { e =>
    val errorHtml = s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"
    sendResponse(exchange, errorHtml)
  }

  // Обработчик страницы действий со студентами
  def studentActionsHandler(exchange: HttpExchange): IO[Unit] =
    readResourceFile("students/studfunc.html")
      .flatMap(sendResponse(exchange, _))
      .handleErrorWith(e => sendResponse(exchange, s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"))

  // Обработчик добавления студента
  def addHandlerStudents(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" => readResourceFile("students/studadd.html").flatMap(sendResponse(exchange, _))

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      student = Student(
        params.getOrElse("name", ""),
        params.getOrElse("group", ""),
        params.get("year").flatMap(y => scala.util.Try(y.toInt).toOption).getOrElse(0),
        params.getOrElse("direction", ""),
        params.getOrElse("faculty", "")
      )
      result <- IO(SQLiteDatabase.insertStudent(student))
      responseHtml = result match {
        case scala.util.Success(_) => "<html><body>Студент добавлен.<br/><a href='/studentActions'>Назад</a></body></html>"
        case scala.util.Failure(ex) => s"<html><body>Ошибка при добавлении: ${escapeHtml(ex.getMessage)}<br/><a href='/studentActions'>Назад</a></body></html>"
      }
      _ <- sendResponse(exchange, responseHtml)
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }

  // Обработчик удаления студента
  def deleteHandlerStudents(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" => for {
      template <- readResourceFile("students/studdelete.html")
      studentsTry = SQLiteDatabase.getAllStudents()
      studentListHtml = studentsTry match {
        case scala.util.Success(students) =>
          students.map(s => s"<li>${escapeHtml(s.name)} - ${escapeHtml(s.group)} (${escapeHtml(s.direction)})</li>").mkString("\n")
        case scala.util.Failure(_) => "<li>Не удалось загрузить список студентов</li>"
      }
      pageHtml = template.replace("{studentList}", studentListHtml)
      _ <- sendResponse(exchange, pageHtml)
    } yield ()

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      name = params.getOrElse("name", "").trim
      _ <- if (name.isEmpty) {
        sendResponse(exchange, "<html><body>Имя не может быть пустым.<br/><a href='/studentActions/delete'>Назад</a></body></html>")
      } else {
        val result = SQLiteDatabase.deleteStudent(name)
        val responseHtml = result match {
          case scala.util.Success(deletedCount) if deletedCount > 0 =>
            s"<html><body>Студент '$name' отчислен.<br/><a href='/studentActions'>Назад</a></body></html>"
          case scala.util.Success(_) =>
            s"<html><body>Студент '$name' не найден.<br/><a href='/studentActions/delete'>Назад</a></body></html>"
          case scala.util.Failure(ex) =>
            s"<html><body>Ошибка при удалении: ${escapeHtml(ex.getMessage)}<br/><a href='/studentActions/delete'>Назад</a></body></html>"
        }
        sendResponse(exchange, responseHtml)
      }
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }

  // Обработчик редактирования студента
  def editHandlerStudents(exchange: HttpExchange): IO[Unit] = exchange.getRequestMethod match {
    case "GET" =>
      readResourceFile("students/studupdate.html").flatMap(sendResponse(exchange, _)).handleErrorWith { e =>
        val errorHtml = s"<html><body><h1>Error: ${escapeHtml(e.getMessage)}</h1></body></html>"
        sendResponse(exchange, errorHtml)
      }

    case "POST" => for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      searchName = params.getOrElse("searchName", "").trim

      _ <- if (searchName.isEmpty) {
        sendResponse(exchange, s"<html><body>Введите имя студента для поиска.<br/><a href='/studentActions/edit'>Назад</a></body></html>")
      } else {
        val studentsTry = SQLiteDatabase.getAllStudents()
        studentsTry match {
          case scala.util.Success(students) =>
            students.find(_.name.equalsIgnoreCase(searchName)) match {
              case Some(orig) =>
                val updated = Student(
                  params.get("name").filter(_.nonEmpty).getOrElse(orig.name),
                  params.get("group").filter(_.nonEmpty).getOrElse(orig.group),
                  params.get("year").flatMap(y => scala.util.Try(y.toInt).toOption).getOrElse(orig.year),
                  params.get("direction").filter(_.nonEmpty).getOrElse(orig.direction),
                  params.get("faculty").filter(_.nonEmpty).getOrElse(orig.faculty)
                )

                val deleteRes = SQLiteDatabase.deleteStudent(orig.name)
                val insertRes = SQLiteDatabase.insertStudent(updated)

                val responseHtml = (deleteRes, insertRes) match {
                  case (scala.util.Success(_), scala.util.Success(_)) =>
                    s"<html><body>Студент обновлён.<br/><a href='/studentActions'>Назад</a></body></html>"
                  case _ =>
                    s"<html><body>Ошибка при обновлении.<br/><a href='/studentActions/edit'>Назад</a></body></html>"
                }
                sendResponse(exchange, responseHtml)

              case None =>
                sendResponse(exchange, s"<html><body>Студент '$searchName' не найден.<br/><a href='/studentActions/edit'>Назад</a></body></html>")
            }
          case scala.util.Failure(_) =>
            sendResponse(exchange, s"<html><body>Ошибка базы данных.<br/><a href='/studentActions/edit'>Назад</a></body></html>")
        }
      }
    } yield ()

    case _ => IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }
  }
}