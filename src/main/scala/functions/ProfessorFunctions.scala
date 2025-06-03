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

object ProfessorFunctions {

  type Log = Vector[String]
  // Тип Writer с логом Log и значением типа A
  type Logged[A] = Writer[Log, A]

  // Поиск преподавателей по выбранному факультету
  def findProfessor(): IO[Unit] = for {
    faculty <- chooseFaculty(faculties)
    profs <- IO.fromTry(SQLiteDatabase.getAllProfessors())
    found = profs.filter(_.faculty == faculty)
    _ <- if (found.isEmpty) printLine("Преподаватели не найдены.")
      else found.traverse_(p =>
        printLine(s"${p.name} – ${p.title}, Email: ${p.email}, Дисциплины: ${p.disciplines.mkString(", ")}")
      )
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()

  // Создание объекта Professor 
  def createProfessor(name: String, title: String, email: String, disciplines: List[String], faculty: String): Either[String, Professor] = for {
    n <- validateNonEmptyGeneric(name, "Имя")
    t <- validateNonEmptyGeneric(title, "Звание")
    e <- validateNonEmptyGeneric(email, "Email")
    d <- validateNonEmptyGeneric(disciplines, "Дисциплины")
    f <- validateNonEmptyGeneric(faculty, "Факультет")
  } yield Professor(n, t, e, d, f)

  // Функция для записи преподавателя в БД с логированием результата
  def insertProfessorLogged(professor: Professor): IO[Logged[Unit]] = IO {
    try {
      insertProfessor(professor)
      Writer(Vector(s"Преподаватель ${professor.name} добавлен."), ())
    } catch {
      case e: Exception => Writer(Vector(s"Ошибка при добавлении: ${e.getMessage}"), ())
    }
  }

  // Обработка результата создания преподавателя
  def handleCreateProfessorResultLogged(name: String, result: Either[String, Professor]): IO[Logged[Unit]] = result match {
    case Right(professor) => insertProfessorLogged(professor)
    case Left(errorMsg) => IO.pure(Writer(Vector(s"Ошибка: $errorMsg"), ()))
  }

  // Добавление преподавателя 
  def insertProfessorToDatabase(): IO[Unit] = for {
    name <- readLine("Введите имя преподавателя: ")
    title <- readLine("Введите звание: ")
    email <- readLine("Введите email: ")
    disciplinesRaw <- readLine("Введите дисциплины через запятую: ")
    disciplines = disciplinesRaw.split(",").map(_.trim).filter(_.nonEmpty).toList
    faculty <- chooseFaculty(faculties)
    result <- IO(createProfessor(name, title, email, disciplines, faculty))
    loggedResult <- handleCreateProfessorResultLogged(name, result)
    (logs, _) = loggedResult.run
    _ <- IO(println(s"Логи для преподавателя $name:"))
    _ <- logs.traverse_(printLine)
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()

  // Создание обновлённого экземпляра Professor 
  def updateProfessor(original: Professor, newTitle: String, newEmail: String, newDisciplines: List[String], newFaculty: String): Either[String, Professor] = for {
    t <- validateNonEmptyGeneric(newTitle, "Звание")
    e <- validateNonEmptyGeneric(newEmail, "Email")
    d <- validateNonEmptyGeneric(newDisciplines, "Дисциплины")
    f <- validateNonEmptyGeneric(newFaculty, "Факультет")
  } yield original.copy(title = t, email = e, disciplines = d, faculty = f)

  // Функция для обновления данных преподавателя
  def handleUpdateProfessorResultLogged(originalName: String, updated: Either[String, Professor]): IO[Logged[Unit]] = updated match {
    case Left(error) =>
      IO.pure(Writer(Vector(s"Ошибка обновления: $error"), ()))
    case Right(updatedProfessor) => IO {
      try {
        deleteProfessor(originalName)
        insertProfessor(updatedProfessor)
        Writer(Vector(s"Данные преподавателя $originalName обновлены."), ())
      } catch {
        case e: Exception =>
          Writer(Vector(s"Ошибка при обновлении преподавателя: ${e.getMessage}"), ())
      }
    }
  }

  // Обработчик результата поиска преподавателя
  def handleProfessorSearch[A](maybeProfessor: Option[Professor])(onFound: Professor => IO[A]): IO[Unit] = maybeProfessor match {
    case None =>
      printLine("Преподаватель не найден.")
    case Some(professor) =>
      onFound(professor).void
  }

  // Вывод логов из Writer в IO[Unit]
  def handleUpdateProfessorResultWithPrint(originalName: String, updated: Either[String, Professor]): IO[Unit] = for {
    logged <- handleUpdateProfessorResultLogged(originalName, updated)
    (logs, _) = logged.run
    _ <- logs.traverse_(printLine)
  } yield ()
  
  // Основная функция редактирования преподавателя
  def editProfessorByName(): IO[Unit] = for {
    name <- readLine("Введите имя преподавателя для редактирования: ")
    all  <- IO.fromTry(Try(SQLiteDatabase.getAllProfessors().getOrElse(List.empty)))
    _ <- handleProfessorSearch(all.find(_.name.equalsIgnoreCase(name))) { professor => for {
      disciplinesRaw <- readLine(s"Введите дисциплины через запятую (были: ${professor.disciplines.mkString(", ")}): ")
      newDisciplines = disciplinesRaw.split(",").map(_.trim).filter(_.nonEmpty).toList
      newTitle <- readLine(s"Новое звание (было ${professor.title}): ")
      newEmail <- readLine(s"Новый email (было ${professor.email}): ")
      newFaculty <- chooseFaculty(faculties)
      updated <- IO(updateProfessor(professor, newTitle, newEmail, newDisciplines, newFaculty))
      _ <- handleUpdateProfessorResultWithPrint(professor.name, updated)
    } yield ()
    }
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()

  // Проверка существования преподавателя при удалении
  def validateProfessorExistenceLogged(name: String): IO[Writer[Vector[String], Boolean]] = for {
    all <- IO.fromTry(Try(SQLiteDatabase.getAllProfessors().getOrElse(List.empty)))
    exists = all.exists(_.name.equalsIgnoreCase(name))
    log <- if (!exists) IO.pure(Writer(Vector("Преподаватель не найден."), false))
      else IO.pure(Writer.value[Vector[String], Boolean](true))
  } yield log

  // Подтверждение удаления преподавателя
  def confirmDeletionProfessor(name: String): IO[Boolean] = for {
    confirm <- readLine(s"Вы уверены, что хотите уволить $name? (y/n) ")
  } yield confirm.trim.toLowerCase == "y"

  // Удаление преподавателя с логированием результата
  def deleteProfessorLogged(name: String): IO[Writer[Vector[String], Unit]] = IO {
    try {
      deleteProfessor(name)
      Writer(Vector(s"Преподаватель $name уволен."), ())
    } catch {
      case e: Exception => Writer(Vector(s"Ошибка при удалении: ${e.getMessage}"), ())
    }
  }

  // Основная функция удаления преподавателя
  def deleteProfessorFromDatabase(): IO[Unit] = {
    def handleDeletion(name: String): IO[Unit] = for {
      validateResult <- validateProfessorExistenceLogged(name)
      (logs, exists) = validateResult.run
      _ <- logs.traverse_(printLine)
      _ <- if (!exists) IO.unit
        else for {
          confirmed <- confirmDeletionProfessor(name)
          _ <- if (confirmed) { for {
            deleteResult <- deleteProfessorLogged(name)
            (delLogs, _) = deleteResult.run
            _ <- delLogs.traverse_(printLine)
          } yield ()
        } else printLine("Операция отменена.")
      } yield ()
    } yield ()

    for {
      name <- readLine("Введите имя преподавателя для увольнения: ")
      _ <- if (name.trim.isEmpty) printLine("Имя не может быть пустым.")
          else handleDeletion(name)
      _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
      _ <- readLine("")
    } yield ()
  }
}