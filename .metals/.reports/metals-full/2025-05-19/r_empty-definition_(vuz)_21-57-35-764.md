error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/ProfessorFunctions.scala:isEmpty.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/ProfessorFunctions.scala
empty definition using pc, found symbol in pc: isEmpty.
semanticdb not found
empty definition using fallback
non-local guesses:
	 -Models.found.isEmpty.
	 -Models.found.isEmpty#
	 -Models.found.isEmpty().
	 -Data.found.isEmpty.
	 -Data.found.isEmpty#
	 -Data.found.isEmpty().
	 -SQLiteDatabase.found.isEmpty.
	 -SQLiteDatabase.found.isEmpty#
	 -SQLiteDatabase.found.isEmpty().
	 -found/isEmpty.
	 -found/isEmpty#
	 -found/isEmpty().
	 -scala/Predef.found.isEmpty.
	 -scala/Predef.found.isEmpty#
	 -scala/Predef.found.isEmpty().
offset: 380
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/ProfessorFunctions.scala
text:
```scala
package functions

import Models._
import Data._
import SQLiteDatabase._
import scala.util.{Try, Success, Failure}

object ProfessorFunctions {
def findProfessor(): Unit = {
    val faculty = chooseFaculty(faculties)

    SQLiteDatabase.getAllProfessors() match {
      case Some(professors) =>
        val found = professors.filter(_.faculty == faculty)
        if (found.isEmpty@@)
          println("Преподаватели не найдены.")
        else
          found.foreach { p =>
            println(s"${p.name} - ${p.title}, Email: ${p.email}, Дисциплины: ${p.disciplines.mkString(", ")}")
          }

      case None =>
        println("Ошибка подключения к базе данных.")
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  def createProfessor(name: String, title: String, email: String, disciplines: List[String], faculty: String): Either[String, Professor] = {
  if (name.isEmpty) Left("Имя не может быть пустым.")
  else if (title.isEmpty) Left("Звание не может быть пустым.")
  else if (email.isEmpty) Left("Email не может быть пустым.")
  else if (disciplines.isEmpty) Left("Дисциплины не могут быть пустыми.")
  else Right(Professor(name, title, email, disciplines, faculty))
  }

  def insertProfessorToDatabase(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя преподавателя: ").trim
  val title = scala.io.StdIn.readLine("Введите звание: ").trim
  val email = scala.io.StdIn.readLine("Введите email: ").trim
  val disciplines = scala.io.StdIn.readLine("Введите дисциплины через запятую: ")
    .split(",").map(_.trim).filter(_.nonEmpty).toList
  val faculty = chooseFaculty(faculties)

  createProfessor(name, title, email, disciplines, faculty) match {
    case Right(professor) =>
      Try(insertProfessor(professor)) match {
        case Success(_) => println(s"Преподаватель $name добавлен.")
        case Failure(e) => println(s"Ошибка при добавлении: ${e.getMessage}")
      }
    case Left(error) =>
      println(s"Ошибка: $error")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def updateProfessor(original: Professor, newTitle: String, newEmail: String, newDisciplines: List[String], newFaculty: String): Either[String, Professor] = {
    if (newTitle.isEmpty) Left("Звание не может быть пустым.")
    else if (newEmail.isEmpty) Left("Email не может быть пустым.")
    else if (newDisciplines.isEmpty) Left("Дисциплины не могут быть пустыми.")
    else Right(original.copy(title = newTitle, email = newEmail, disciplines = newDisciplines, faculty = newFaculty))
  }

  def editProfessorByName(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя преподавателя для редактирования: ").trim

  SQLiteDatabase.getAllProfessors() match {
    case Some(all) =>
      all.find(_.name.equalsIgnoreCase(name)) match {
        case Some(professor) =>
          val newTitle = scala.io.StdIn.readLine(s"Новое звание (было ${professor.title}): ").trim
          val newEmail = scala.io.StdIn.readLine(s"Новый email (было ${professor.email}): ").trim
          val newDisciplines = scala.io.StdIn
            .readLine(s"Введите дисциплины через запятую (были: ${professor.disciplines.mkString(", ")}): ")
            .split(",").map(_.trim).filter(_.nonEmpty).toList
          val newFaculty = chooseFaculty(faculties)

          updateProfessor(professor, newTitle, newEmail, newDisciplines, newFaculty) match {
            case Right(updated) =>
              deleteProfessor(professor.name)
              insertProfessor(updated)
              println("Данные обновлены.")
            case Left(error) =>
              println(s"Ошибка: $error")
          }

        case None =>
          println("Преподаватель не найден.")
      }

    case None =>
      println("Ошибка при получении данных из БД.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def removeProfessor(name: String, confirm: String): Boolean =
  confirm.trim.toLowerCase == "y" && name.nonEmpty  

  def deleteProfessorFromDatabase(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя преподавателя для удаления: ").trim
  if (name.isEmpty) return println("Имя не может быть пустым.")

  SQLiteDatabase.getAllProfessors() match {
    case Some(all) =>
      if (!all.exists(_.name.equalsIgnoreCase(name)))
        return println("Преподаватель не найден.")

      println(s"Вы уверены, что хотите удалить $name? (y/n)")
      val confirm = scala.io.StdIn.readLine()

      if (removeProfessor(name, confirm)) {
        deleteProfessor(name)
        println(s"Преподаватель $name удалён.")
      } else {
        println("Удаление отменено.")
      }

    case None =>
      println("Ошибка подключения к базе данных.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: isEmpty.