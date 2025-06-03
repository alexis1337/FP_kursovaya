import scala.util.{Try, Success, Failure}
import cats.effect._
import cats.implicits._
import cats.data.StateT
import cats.effect.IO
import cats.data.Writer
import cats.instances.vector._
import cats.syntax.writer._

import Data._
import Models._
import SQLiteDatabase._
import Utils._ 

object StudentFunctions {

  type Log = Vector[String]
  type Logged[A] = Writer[Log, A]

  // Поиск
  def showStudentSearch(): IO[Unit] = for {
    faculty <- chooseFaculty(faculties)
    year <- readInt("Введите год поступления: ", 2021, 2024)
    students <- IO.fromTry(Try(SQLiteDatabase.getAllStudents().get))
    found = students.filter(s => s.faculty == faculty && s.year == year)
    _ <- if (found.isEmpty) printLine("Студенты не найдены.")
      else printLine(found.map(s => s"${s.name} - ${s.group} (${s.direction})").mkString("\n"))
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()

  // Создание экземпляра 
  def createStudent(name: String, group: String, year: Int, direction: String, faculty: String): Either[String, Student] = for {
    n <- validateNonEmptyGeneric(name, "Имя")
    g <- validateNonEmptyGeneric(group, "Группа")
    d <- validateNonEmptyGeneric(direction, "Направление")
    f <- validateNonEmptyGeneric(faculty, "Факультет")
  } yield Student(n, g, year, d, f)

  // Вспомогательная функция для обработки результата создания студента
  def insertStudentLogged(student: Student): IO[Logged[Unit]] = IO {
    try {
      insertStudent(student)
      Writer(Vector(s"Студент ${student.name} добавлен в базу данных."), ())
    } catch {
      case e: Exception => Writer(Vector(s"Ошибка при добавлении: ${e.getMessage}"), ())
    }
  }

  def handleCreateStudentResultLogged(name: String, result: Either[String, Student]): IO[Logged[Unit]] = result match {
    case Right(student) => insertStudentLogged(student)
    case Left(errorMsg) => IO.pure(Writer(Vector(s"Ошибка: $errorMsg"), ()))
  }
  
  // Добавление в БД через ввод
  def insertStudentToDatabase(): IO[Unit] = for {
    name <- readLine("Введите имя студента: ")
    group <- readLine("Введите группу: ")
    year <- readInt("Введите год поступления: ", 2021, 2024)
    direction <- readLine("Введите направление: ")
    faculty <- chooseFaculty(faculties)
    result <- IO(createStudent(name, group, year, direction, faculty))
    loggedResult <- handleCreateStudentResultLogged(name, result)
    (logs, _) = loggedResult.run
    _ <- logs.traverse_(printLine)
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()

  // Веб-версия insertStudent: принимает все параметры явно, возвращает лог
  def insertStudentWeb(name: String, group: String, year: Int, direction: String, faculty: String): IO[Logged[Unit]] = {
    createStudent(name, group, year, direction, faculty) match {
      case Right(student) => insertStudentLogged(student)
      case Left(error)    => IO.pure(Writer(Vector(s"Ошибка: $error"), ()))
    }
  }

  // Новая версия с изменёнными полями  
  def updateStudent(original: Student, newGroup: String, newYear: Int, newDirection: String, newFaculty: String): Either[String, Student] = for {
    g <- validateNonEmptyGeneric(newGroup, "Группа")
    d <- validateNonEmptyGeneric(newDirection, "Направление")
    f <- validateNonEmptyGeneric(newFaculty, "Факультет")
  } yield original.copy(group = g, year = newYear, direction = d, faculty = f)

  // Обработка Either с логированием через Writer
  def handleUpdateStudentResultLogged(originalName: String, updated: Either[String, Student]): IO[Logged[Unit]] = updated match {
    case Left(error) =>
      IO.pure(Writer(Vector(s"Ошибка обновления: $error"), ()))
    case Right(updatedStudent) => IO {
      try {
        deleteStudent(originalName)
        insertStudent(updatedStudent)
        Writer(Vector(s"Данные студента $originalName обновлены."), ())
      } catch {
        case e: Exception =>
          Writer(Vector(s"Ошибка при обновлении студента: ${e.getMessage}"), ())
      }
    }
  }

  // Обертка, чтобы вернуть IO[Unit] и сразу вывести логи
  def handleUpdateStudentResultWithPrint(originalName: String, updated: Either[String, Student]): IO[Unit] = for {
    logged <- handleUpdateStudentResultLogged(originalName, updated)
    (logs, _) = logged.run
    _ <- logs.traverse_(printLine)
  } yield ()

  // Обработка Option с передачей действия на найденного студента
  def handleStudentSearch[A](maybeStudent: Option[Student])(onFound: Student => IO[A]): IO[Unit] = maybeStudent match {
    case None => printLine("Студент не найден.")
    case Some(student) => onFound(student).void
  }

  // Редактирование
  def editStudentByName(): IO[Unit] = for {
    name <- readLine("Введите имя студента для редактирования: ")
    all <- IO.fromTry(Try(SQLiteDatabase.getAllStudents().get))
    _ <- handleStudentSearch(all.find(_.name.equalsIgnoreCase(name))) { student => for {
      newGroup <- readLine(s"Новая группа (было ${student.group}): ")
      newYear <- readInt(s"Новый год поступления (было ${student.year}): ", 2021, 2024)
      newDirection <- readLine(s"Новое направление (было ${student.direction}): ")
      newFaculty <- chooseFaculty(faculties)
      updated = updateStudent(student, newGroup, newYear, newDirection, newFaculty)
      _ <- handleUpdateStudentResultWithPrint(student.name, updated)
    } yield ()
    }
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()

  // Вспомогательная функция логированного удаления студента
  def deleteStudentLogged(name: String): IO[Logged[Unit]] = IO {
    try {
      deleteStudent(name)
      Writer(Vector(s"Студент $name отчислен."), ())
    } catch {
      case e: Exception => Writer(Vector(s"Ошибка при удалении студента: ${e.getMessage}"), ())
    }
  }

  // Проверка существования студента с логом, если не найден
  def validateStudentExistenceLogged(name: String): IO[Writer[Vector[String], Boolean]] = for {
    all <- IO.fromTry(Try(SQLiteDatabase.getAllStudents().get))
    exists = all.exists(_.name.equalsIgnoreCase(name))
    log <- if (!exists) IO.pure(Writer(Vector("Студент не найден."), false))
      else IO.pure(Writer.value[Vector[String], Boolean](true))
  } yield log

  // Подтверждение удаления (без логирования)
  def confirmDeletionStudent(name: String): IO[Boolean] = for {
    confirm <- readLine(s"Вы уверены, что хотите отчислить $name? (y/n) ")
  } yield confirm.trim.toLowerCase == "y"

  // Удаление студента с логами
  def deleteStudentFromDatabase(): IO[Unit] = {
    def handleDeletion(name: String): IO[Unit] = for {
      validateResult <- validateStudentExistenceLogged(name)
      (logs, exists) = validateResult.run
      _ <- logs.traverse_(printLine)
      _ <- if (!exists) IO.unit
        else for {
          confirmed <- confirmDeletionStudent(name)
          _ <- if (confirmed) { for {
            deleteResult <- deleteStudentLogged(name)
            (delLogs, _) = deleteResult.run
            _ <- delLogs.traverse_(printLine)
          } yield ()
        } else printLine("Операция отменена.")
        } yield ()
    } yield ()

    for {
      name <- readLine("Введите имя студента для отчисления: ")
      _ <- if (name.trim.isEmpty) printLine("Имя не может быть пустым.")
        else handleDeletion(name)
      _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
      _ <- readLine("")
    } yield ()
  }
}