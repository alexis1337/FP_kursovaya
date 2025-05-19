error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/TimetableFunctions.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/TimetableFunctions.scala
empty definition using pc, found symbol in pc: `<none>`.
semanticdb not found
empty definition using fallback
non-local guesses:
	 -Models.Models.
	 -Data.Models.
	 -SQLiteDatabase.Models.
	 -Models.
	 -scala/Predef.Models.
offset: 27
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/TimetableFunctions.scala
text:
```scala
package functions

import M@@odels._
import Data._
import SQLiteDatabase._
import scala.util.{Try, Success, Failure}

def showTimetable(): Unit = {
  val group = scala.io.StdIn.readLine("Введите название группы (например, 22-ИВТ-1): ").trim

  SQLiteDatabase.getTimetableByGroup(group) match {
    case Some(entries) if entries.nonEmpty =>
      println(s"\nРасписание для группы $group:")
      entries.foreach { e =>
        println(s"${e.day}, ${e.time} — ${e.subject} (${e.room})")
      }
    case Some(_) =>
      println("Расписание для этой группы пустое.")
    case None =>
      println("Ошибка при подключении к базе данных или группа не найдена.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def createTimetableEntry(day: String, time: String, subject: String, room: String): Either[String, TimetableEntry] = {
  if (day.isEmpty) Left("День недели не может быть пустым.")
  else if (time.isEmpty) Left("Время не может быть пустым.")
  else if (subject.isEmpty) Left("Название предмета не может быть пустым.")
  else if (room.isEmpty) Left("Аудитория не может быть пустой.")
  else Right(TimetableEntry(day, time, subject, room))
}
  
  def insertTimetableToDatabase(): Unit = {
  val group = scala.io.StdIn.readLine("Введите название группы: ").trim
  val daysOfWeek = List("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")

  def loop(): Unit = {
    println("\nВыберите день недели:")
    daysOfWeek.zipWithIndex.foreach { case (d, i) => println(s"${i + 1}. $d") }
    println(s"${daysOfWeek.length + 1}. Завершить ввод")

    val choice = readInt("Введите номер: ", 1, daysOfWeek.length + 1)
    if (choice == daysOfWeek.length + 1) {
      println("Ввод завершён.")
    } else {
      val day = daysOfWeek(choice - 1)
      val count = readInt(s"Сколько пар добавить на $day?", 1, 8)

      (1 to count).foreach { _ =>
        val time = scala.io.StdIn.readLine("Время пары: ").trim
        val subject = scala.io.StdIn.readLine("Предмет: ").trim
        val room = scala.io.StdIn.readLine("Аудитория: ").trim

        createTimetableEntry(day, time, subject, room) match {
          case Right(entry) =>
            Try(SQLiteDatabase.insertTimetableEntry(entry, group)) match {
              case Success(_) => println(s"Пара $subject добавлена.")
              case Failure(e) => println(s"Ошибка при добавлении: ${e.getMessage}")
            }
          case Left(error) =>
            println(s"Ошибка ввода: $error")
        }
      }

      loop()
    }
  }

  loop()
  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def editTimetable(): Unit = {
    val group = scala.io.StdIn.readLine("Введите название группы для редактирования: ").trim

    SQLiteDatabase.getTimetableByGroup(group) match {
      case Some(existing) if existing.nonEmpty =>
        println(s"\nТекущее расписание для группы $group:")
        existing.foreach(e => println(s"${e.day}, ${e.time} — ${e.subject} (${e.room})"))

        val daysOfWeek = List("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")

        def loop(): Unit = {
          println("\nВыберите день недели для редактирования:")
          daysOfWeek.zipWithIndex.foreach { case (d, i) => println(s"${i + 1}. $d") }
          println(s"${daysOfWeek.length + 1}. Завершить редактирование")

          val choice = readInt("Введите номер: ", 1, daysOfWeek.length + 1)

          if (choice == daysOfWeek.length + 1) {
            println(s"\nРасписание для группы $group обновлено.")
          } else {
            val day = daysOfWeek(choice - 1)

            Try(SQLiteDatabase.deleteTimetableByGroupAndDay(group, day)) match {
              case Success(_) =>
                val count = readInt(s"Сколько пар хотите ввести на $day?", 0, 8)

                (1 to count).foreach { _ =>
                  val time = scala.io.StdIn.readLine("Время пары (например, 08:00-09:35): ").trim
                  val subject = scala.io.StdIn.readLine("Название предмета: ").trim
                  val room = scala.io.StdIn.readLine("Аудитория: ").trim

                  Try(SQLiteDatabase.insertTimetableEntry(TimetableEntry(day, time, subject, room), group)) match {
                    case Success(_) => // можно вывести сообщение о добавлении пары
                    case Failure(e) => println(s"Ошибка при добавлении пары: ${e.getMessage}")
                  }
                }

                println(s"День $day обновлён.")
                loop()

              case Failure(e) =>
                println(s"Ошибка при удалении расписания на $day: ${e.getMessage}")
            }
          }
        }

        loop()

      case Some(_) =>
        println("Расписание для этой группы пустое.")

      case None =>
        println("Ошибка подключения к базе данных или группа не найдена.")
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  def deleteTimetableToDatabase(): Unit = {
  val group = scala.io.StdIn.readLine("Введите группу: ").trim

  SQLiteDatabase.getTimetableByGroup(group) match {
    case Some(entries) if entries.nonEmpty =>
      println(s"Вы уверены, что хотите удалить расписание для группы $group? (y/n)")
      val confirm = scala.io.StdIn.readLine().trim.toLowerCase
      if (confirm == "y") {
        SQLiteDatabase.deleteTimetableByGroup(group)
        println("Расписание удалено.")
      } else println("Операция отменена.")
    case Some(_) =>
      println("Расписание уже пустое.")
    case None =>
      println("Ошибка подключения или группа не найдена.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.