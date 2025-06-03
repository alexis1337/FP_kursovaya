import cats.implicits._
import cats.data.StateT
import cats.effect.IO
import cats.data.Writer

import Models._
import StudentFunctions._ 
import ProfessorFunctions._ 
import TimetableFunctions._ 

object Menus {
  
  // Для накопления сообщений
  type Log = Vector[String]
  // StateT с состоянием MenuState и эффектом IO
  type MenuStateT[A] = StateT[IO, MenuState, A]

  // Далее - контекст MenuStateT
  def printLineT(msg: String): MenuStateT[Unit] = StateT.liftF(IO(println(msg)))
  def readLineT(prompt: String): MenuStateT[String] = StateT.liftF(IO {
    print(prompt)
    scala.io.StdIn.readLine()
  })
  def selectOption(option: String): MenuStateT[Unit] = StateT.modify(_.copy(selectedOption = option))
  def getSelectedOption: MenuStateT[String] = StateT.inspect(_.selectedOption)

  // Последовательность кортежей (номер, описание, действие)
  def runSubMenu(options: Seq[(String, String, MenuStateT[Unit])], menuTitle: String): MenuStateT[Unit] = {
    def loop(): MenuStateT[Unit] = for {
      _ <- printLineT(s"\n=== $menuTitle ===")
      _ <- options.traverse_ { case (num, desc, _) => printLineT(s"$num. $desc") }
      inp <- readLineT("Выберите действие: ")
      _ <- selectOption(inp.trim)
      opt <- getSelectedOption
      _ <- options.find(_._1 == opt) match {
        case Some((_, _, action)) if opt == options.last._1 => StateT.liftF(IO.unit) // выход по последнему пункту
        case Some((_, _, action)) => action >> loop()
        case None => printLineT("Неверный ввод.") >> loop()
      }
    } yield ()

    loop()
  }

  def studentActionsMenu(): MenuStateT[Unit] = {
    val options = Seq(
      ("1", "Добавить студента", StateT.liftF(insertStudentToDatabase())),
      ("2", "Отчислить студента", StateT.liftF(deleteStudentFromDatabase())),
      ("3", "Редактировать студента", StateT.liftF(editStudentByName())),
      ("4", "Назад", StateT.pure[IO, MenuState, Unit](()))
    )
    runSubMenu(options, "Действия над студентом")
  }

  def professorActionsMenu(): MenuStateT[Unit] = {
    val options = Seq(
      ("1", "Добавить преподавателя", StateT.liftF(insertProfessorToDatabase())),
      ("2", "Уволить преподавателя", StateT.liftF(deleteProfessorFromDatabase())),
      ("3", "Редактировать преподавателя", StateT.liftF(editProfessorByName())),
      ("4", "Назад", StateT.pure[IO, MenuState, Unit](()))
    )
    runSubMenu(options, "Действия над преподавателем")
  }

  def timeTableActionsMenu(): MenuStateT[Unit] = {
    val options = Seq(
      ("1", "Добавить расписание", StateT.liftF(insertTimetableToDatabase())),
      ("2", "Удалить расписание", StateT.liftF(deleteTimetableToDatabase())),
      ("3", "Редактировать расписание", StateT.liftF(editTimetable())),
      ("4", "Назад", StateT.pure[IO, MenuState, Unit](()))
    )
    runSubMenu(options, "Действия над расписанием")
  }
}