import cats.data.StateT
import cats.effect.{IO, IOApp}
import scala.util.chaining._
import cats.implicits._

import SQLiteDatabase._
import Models._
import StudentFunctions._
import ProfessorFunctions._
import TimetableFunctions._
import Applicant._
import Menus._
import Utils._

object Main extends IOApp.Simple {

  // Начальное состояние меню — хранит выбранный пункт
  val initialMenuState = MenuState("")

  // Универсальная обёртка для запуска подменю с состоянием
  def runMenu(menu: MenuStateT[?]): IO[Unit] =
    menu.runA(initialMenuState).void

  // Главное меню с опциями и действиями
  val menuOptions = Seq(
    "1" -> ("Найти студента", () => showStudentSearch()),
    "2" -> ("Найти преподавателя", () => findProfessor()),
    "3" -> ("Посмотреть расписание", () => showTimetable()),
    "4" -> ("Меню абитуриента", () => showApplicantMenu()),
    "5" -> ("Действия над студентом", () => runMenu(studentActionsMenu())),
    "6" -> ("Действия над преподавателем", () => runMenu(professorActionsMenu())),
    "7" -> ("Действия над расписанием", () => runMenu(timeTableActionsMenu())),
    "0" -> ("Выход", () => IO.println("До свидания!"))
  )

  // Отображение меню и чтение выбора пользователя
  def showMenu: IO[Option[(String, String, () => IO[Unit])]] = for {
    _   <- printLine("\n=== Справочная система ВУЗа ===")
    _   <- menuOptions.traverse { case (k, (desc, _)) => printLine(s"$k. $desc") }
    inp <- readLine("Выберите пункт: ")
    res = menuOptions.find(_._1 == inp.trim).map { case (k, (desc, act)) => (k, desc, act) }
  } yield res

  // Основной цикл обработки пользовательских команд
  def mainLoop: IO[Unit] =
    showMenu.flatMap {
      case Some((key, _, action)) if key != "0" =>
        action() >> mainLoop
      case Some((_, _, action)) =>
        action()
      case None =>
        for {
          _ <- printLine("Неверный ввод.")
          _ <- printLine("\nНажмите Enter, чтобы продолжить...")
          _ <- readLine("")
          _ <- mainLoop
        } yield ()
    }

  // Точка входа
  val run: IO[Unit] = for {
    _ <- IO(createStudentsTable())
    _ <- IO(createProfessorsTable())
    _ <- IO(createTimetableTable())
    _ <- mainLoop
  } yield ()
}
