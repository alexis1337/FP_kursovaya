import Functions._
import Models._

object Main {
  def main(args: Array[String]): Unit = {
    // Создаем таблицу, если она еще не существует
    SQLiteDatabase.createStudentsTable()
    SQLiteDatabase.createProfessorsTable()
    SQLiteDatabase.createTimetableTable()
    mainLoop()  // Запуск основного цикла
  }

  def mainLoop(): Unit = {
  val menuOptions = Seq(
    ("1", "Найти студента", () => showStudentSearch()),
    ("2", "Найти преподавателя", () => findProfessor()),
    ("3", "Посмотреть расписание", () => showTimetable()),
    ("4", "Меню абитуриента", () => showApplicantMenu()),
    ("5", "Действие над студентом", () => studentActionsMenu()),     
    ("6", "Действие над преподавателем", () => professorActionsMenu()),
    ("7", "Действие над расписанием", () => timeTableActionsMenu()),    
    ("0", "Выход", () => println("До свидания!"))
  )

  // Рекурсивный цикл главного меню

  def loop(): Unit = {
    val selected = showMenu(menuOptions)
    selected match {
      case Some(("0", _, action)) =>
        action()
      case Some((_, _, action)) =>
        action()
        loop()
      case None =>
        println("Неверный ввод.")
        println("\nНажмите Enter, чтобы продолжить...")
        scala.io.StdIn.readLine()
        loop()
    }
  }

  loop()
}

  // Отображение списка
  
  def showMenu(options: Seq[(String, String, () => Unit)]): Option[(String, String, () => Unit)] = {
    Iterator.continually {
      println("\n=== Справочная система ВУЗа ===")
      options.foreach { case (number, description, _) => println(s"$number. $description") }
      val input = scala.io.StdIn.readLine("Выберите пункт: ").trim
      options.find(_._1 == input)
    }.find(_.isDefined).flatten
  }
}