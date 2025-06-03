import Data._
import Models._
import SQLiteDatabase._
import Utils._
import scala.util.{Try, Success, Failure}
import cats.effect._
import cats.implicits._
import cats.data.Writer
import cats.instances.vector._
import cats.syntax.writer._

object TimetableFunctions {

  type Logged[A] = Writer[Vector[String], A]

  // Отображение расписания с обработкой Try
  def handleShowTimetable(entriesTry: Try[List[TimetableEntry]], group: String): IO[Unit] = entriesTry match {
    case Success(entries) if entries.nonEmpty =>
      printLine(s"\nРасписание для группы $group:") >>
        entries.traverse_(e => printLine(s"${e.day}, ${e.time} — ${e.subject} (${e.room})"))
    case Success(_) =>
      printLine("Расписание для этой группы пустое.")
    case Failure(_) =>
      printLine("Группа не найдена или ошибка БД.")
  }

  // Показ расписания
  def showTimetable(): IO[Unit] = for {
    group <- readLine("Введите название группы (например, 22-ИВТ-3): ")
    entriesTry = Try(SQLiteDatabase.getTimetableByGroup(group.trim).getOrElse(Nil))
    _ <- handleShowTimetable(entriesTry, group)
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()

  // Создание записи расписания с валидацией
  def createTimetableEntry(day: String, time: String, subject: String, room: String): Either[String, TimetableEntry] = for {
    d <- validateNonEmptyGeneric(day, "День недели")
    t <- validateNonEmptyGeneric(time, "Время")
    s <- validateNonEmptyGeneric(subject, "Название предмета")
    r <- validateNonEmptyGeneric(room, "Аудитория")
  } yield TimetableEntry(d, t, s, r)

  // Логированное добавление записи расписания
  def insertTimetableEntryLogged(entry: TimetableEntry, group: String): IO[Logged[Unit]] = IO {
    try {
      SQLiteDatabase.insertTimetableEntry(entry, group)
      Writer(Vector(s"Пара ${entry.subject} добавлена."), ())
    } catch {
      case e: Exception => Writer(Vector(s"Ошибка при добавлении: ${e.getMessage}"), ())
    }
  }

  // Обработка результата создания записи с логированием
  def handleEntryInsertLogged(entryResult: Either[String, TimetableEntry], group: String): IO[Logged[Unit]] = entryResult match {
    case Right(entry) => insertTimetableEntryLogged(entry, group)
    case Left(error) => IO.pure(Writer(Vector(s"Ошибка ввода: $error"), ()))
  }

  // Добавление расписания в БД с циклом по дням и парам
  def insertTimetableToDatabase(): IO[Unit] = {
    val daysOfWeek = List("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")

    def loop(group: String): IO[Unit] =
      printLine("\nВыберите день недели:") >> daysOfWeek.zipWithIndex.traverse_ { case (d, i) => printLine(s"${i + 1}. $d") } >>
        printLine(s"${daysOfWeek.length + 1}. Завершить ввод") >> readLine("Введите номер: ").flatMap { choiceStr =>
        val choice = choiceStr.toIntOption.getOrElse(-1)
        if (choice == daysOfWeek.length + 1) printLine("Ввод завершён.")
        else if (choice >= 1 && choice <= daysOfWeek.length) {
          val day = daysOfWeek(choice - 1)
          readLine(s"Сколько пар добавить на $day? ").flatMap { countStr =>
            val count = countStr.toIntOption.getOrElse(0)
            (1 to count).toList.traverse_ { _ => for {
              time <- readLine("Время пары: ")
              subject <- readLine("Предмет: ")
              room <- readLine("Аудитория: ")
              entryResult = createTimetableEntry(day, time, subject, room)
              loggedResult <- handleEntryInsertLogged(entryResult, group)
              (logs, _) = loggedResult.run
              _ <- logs.traverse_(printLine)
            } yield ()
            } >> loop(group)
          }
        } else
          printLine("Неверный ввод.") >> loop(group)
      }

    for {
      group <- readLine("Введите название группы: ")
      _ <- loop(group.trim)
      _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
      _ <- readLine("")
    } yield ()
  }

  // Редактирование расписания с циклом по дням
  def editTimetable(): IO[Unit] = {
    val daysOfWeek = List("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")

    def loop(group: String): IO[Unit] = {
      def printSchedule(entries: List[TimetableEntry]): IO[Unit] =
        printLine(s"\nТекущее расписание для группы $group:") >>
          entries.traverse_(e => printLine(s"${e.day}, ${e.time} — ${e.subject} (${e.room})"))

      def chooseDay(): IO[Int] =
        printLine("\nВыберите день недели для редактирования:") >>
          daysOfWeek.zipWithIndex.traverse { case (d, i) => printLine(s"${i + 1}. $d") } >>
          printLine(s"${daysOfWeek.length + 1}. Завершить редактирование") >>
          readLine("Введите номер: ").map(s => s.toIntOption.getOrElse(-1))

      def inputPairs(day: String): IO[Unit] = {
        readLine(s"Сколько пар хотите ввести на $day? ").flatMap { countStr => val count = countStr.toIntOption.getOrElse(0)

        def insertOnePair(): IO[Unit] = for {
          time <- readLine("Время пары (например, 08:00-09:35): ")
          subject <- readLine("Название предмета: ")
          room <- readLine("Аудитория: ")
          res <- IO(Try(SQLiteDatabase.insertTimetableEntry(TimetableEntry(day, time, subject, room), group)))
          _ <- res match {
            case Success(_) => IO.unit
            case Failure(e) => printLine(s"Ошибка при добавлении пары: ${e.getMessage}")
          }
        } yield ()

        List.fill(count)(insertOnePair()).foldLeft(IO.unit)(_ >> _) >> printLine(s"День $day обновлён.")
         }
      }

      def processDayChoice(choice: Int): IO[Unit] =
        if (choice == daysOfWeek.length + 1) printLine(s"\nРасписание для группы $group обновлено.")
        else if (choice >= 1 && choice <= daysOfWeek.length) {
          val day = daysOfWeek(choice - 1)
          IO(Try(SQLiteDatabase.deleteTimetableByGroupAndDay(group, day))).flatMap {
            case Success(_) => inputPairs(day) >> loop(group)
            case Failure(e) => printLine(s"Ошибка при удалении расписания на $day: ${e.getMessage}") >> loop(group)
          }
        } else printLine("Неверный ввод.") >> loop(group)

      IO.fromTry(SQLiteDatabase.getTimetableByGroup(group)).flatMap {
        case entries if entries.nonEmpty =>
          printSchedule(entries)
        case _ =>
          IO.println("Расписание отсутствует")
      }.handleErrorWith { ex =>
        IO.println(s"Ошибка при загрузке расписания: ${ex.getMessage}")
      }
    }

    for {
      group <- readLine("Введите название группы для редактирования: ")
      _ <- loop(group.trim)
      _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
      _ <- readLine("")
    } yield ()
  }

  // Обработка результата удаления расписания
  def handleDeleteTimetableResult(entriesTry: Try[List[TimetableEntry]], group: String): IO[Unit] = entriesTry match {
    case Success(entries) if entries.nonEmpty => for {
      confirm <- readLine(s"Хотите удалить расписание для группы $group? (y/n) ")
      _ <- if (confirm.trim.toLowerCase == "y")
        IO(SQLiteDatabase.deleteTimetableByGroup(group)) >> printLine("Расписание удалено.")
      else
        printLine("Операция отменена.")
    } yield ()

    case Success(_) =>
      printLine("Расписание уже пустое.")

    case Failure(_) =>
      printLine("Группа не найдена или ошибка БД.")
  }

  // Удаление расписания из БД
  def deleteTimetableToDatabase(): IO[Unit] = for {
    group <- readLine("Введите группу: ")
    entriesTry = SQLiteDatabase.getTimetableByGroup(group.trim).map(Success(_)).getOrElse(Failure(new Exception("Нет таких групп")))
    _ <- handleDeleteTimetableResult(entriesTry, group)
    _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
    _ <- readLine("")
  } yield ()
}