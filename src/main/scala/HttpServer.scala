import com.sun.net.httpserver.{HttpExchange, HttpServer} // HttpExchange — используется для чтения данных запроса и отправки ответа
import java.net.InetSocketAddress // Для работы с IP-адресом и портом сервера
import cats.effect.unsafe.implicits.global
import cats.effect.IO
import cats.data.Writer
import cats.syntax.writer._

import Models._
import SQLiteDatabase._
import HandlersForStudents._
import HandlersForProfessors._
import HandlersForTimetable._
import HandlersForApplicant._
import HandlersForMenu._
import HttpUtils._ 

object HttpServerApp {

  type Logged[A] = Writer[List[String], A]
  
  // Вспомогательная функция для запуска HttpExchange обработчиков в IO
  def toHandler(ioHandler: HttpExchange => IO[Unit])(exchange: HttpExchange): Unit = {
    // Возвращает Writer с логами
    val loggedIO: IO[Logged[Unit]] = for {
      _ <- IO.pure(Writer.tell(List(s"${exchange.getRequestURI}")))
      _ <- ioHandler(exchange)
    } yield Writer.value(())

    // Запускаем эффект синхронно
    val logged: Logged[Unit] = loggedIO.unsafeRunSync()

    // Выводим все собранные логи
    logged.written.foreach(println)
  }

  def main(args: Array[String]): Unit = {
    // Создаем таблицы при старте
    createStudentsTable()
    createProfessorsTable()
    createTimetableTable()

    // Создаём сервак
    val server = HttpServer.create(new InetSocketAddress(8080), 0)

    // Редирект с корня на меню
    server.createContext("/", toHandler(redirectToMenuHandler))

    // Для меню
    server.createContext("/menu", toHandler(menuHandler))

    // Для студентов
    server.createContext("/students", toHandler(studentsHandler))
    server.createContext("/studentActions", toHandler(studentActionsHandler))
    server.createContext("/studentActions/add", toHandler(addHandlerStudents))
    server.createContext("/studentActions/delete", toHandler(deleteHandlerStudents))
    server.createContext("/studentActions/edit", toHandler(editHandlerStudents))

    // Для преподавателей
    server.createContext("/professors", toHandler(professorsHandler))
    server.createContext("/professorActions", toHandler(professorActionsHandler))
    server.createContext("/professorActions/add", toHandler(addHandlerProfessors))
    server.createContext("/professorActions/delete", toHandler(deleteHandlerProfessors))
    server.createContext("/professorActions/edit", toHandler(editHandlerProfessors))

    // Для расписания
    server.createContext("/timetable", toHandler(timetableHandler))
    server.createContext("/timetableActions", toHandler(timetableActionsHandler))
    server.createContext("/timetableActions/add", toHandler(addHandler))
    server.createContext("/timetableActions/delete", toHandler(deleteHandler))
    server.createContext("/timetableActions/edit", toHandler(editHandler))

    // Для абитуриентов
    server.createContext("/applicant", toHandler(applicantHandler))

    server.setExecutor(null) // Используем стандартный 1-поточный executor
    server.start()
    println("Сервер запущен на http://localhost:8080/")
  }
}