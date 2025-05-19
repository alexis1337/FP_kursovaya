error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala:copy.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
empty definition using pc, found symbol in pc: copy.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 6618
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
text:
```scala
import Data._
import Models._
import SQLiteDatabase._
import scala.util.{Try, Success, Failure}

object Functions {

  def readInt(prompt: String, min: Int = Int.MinValue, max: Int = Int.MaxValue): Int = {
  Iterator.continually {
    println(prompt)
    val input = scala.io.StdIn.readLine()
    input.toIntOption match {
      case Some(num) if num >= min && num <= max => Some(num)
      case _ =>
        println(s"Ошибка: введите число от $min до $max.")
        None
    }
  }.collectFirst { case Some(value) => value }.get
}

  def chooseFaculty(faculties: List[String]): String = {
    Iterator.continually {
      faculties.zipWithIndex.foreach { case (facultyName, index) => println(s"${index + 1}. $facultyName") }
      val index = readInt("Введите номер факультета: ", 1, faculties.length)
      faculties(index - 1)
    }.next()
  }

  def isConnected: Boolean = {
    Try(getAllStudents()).isSuccess
  }

  def showStudentSearch(): Unit = {
  val faculty = chooseFaculty(faculties)
  val year = readInt("Введите год поступления: ", 2021, 2024)

  SQLiteDatabase.getAllStudents() match {
    case Success(students) =>
      val found = students.filter(s => s.faculty == faculty && s.year == year)
      if (found.isEmpty) println("Студенты не найдены.")
      else found.foreach(s => println(s"${s.name} - ${s.group} (${s.direction})"))

    case Failure(ex) =>
      println(s"Ошибка при подключении к базе данных: ${ex.getMessage}")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def insertStudentToDatabase(): Unit = {
    if (!isConnected) throw new NullPointerException("База данных недоступна")

    val name = scala.io.StdIn.readLine("Введите имя студента: ").trim
    if (name.isEmpty) return println("Имя не может быть пустым.")

    val group = scala.io.StdIn.readLine("Введите группу: ").trim
    if (group.isEmpty) return println("Группа не может быть пустой.")

    val year = readInt("Введите год поступления: ", 2021, 2024)

    val direction = scala.io.StdIn.readLine("Введите направление: ").trim
    if (direction.isEmpty) return println("Направление не может быть пустым.")

    val faculty = chooseFaculty(faculties)

    val student = Student(name, group, year, direction, faculty)

    Try(insertStudent(student)) match {
      case Success(_) => println(s"Студент $name добавлен в базу данных.")
      case Failure(e) => println(s"Ошибка при добавлении: ${e.getMessage}")
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  def editStudentByName(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя студента для редактирования: ").trim

  SQLiteDatabase.getAllStudents() match {
    case Success(all) =>
      all.find(_.name.equalsIgnoreCase(name)) match {
        case Some(student) =>
          val newGroup = scala.io.StdIn.readLine(s"Введите новую группу (было ${student.group}): ").trim
          val newYear = readInt(s"Введите новый год поступления (было ${student.year}): ", 2021, 2024)
          val newDirection = scala.io.StdIn.readLine(s"Введите новое направление (было ${student.direction}): ").trim
          val newFaculty = chooseFaculty(faculties)

          deleteStudent(student.name)
          insertStudent(student.copy(group = newGroup, year = newYear, direction = newDirection, faculty = newFaculty))

          println("Данные обновлены.")
        case None =>
          println("Студент не найден.")
      }

    case Failure(ex) =>
      println(s"Ошибка при получении данных: ${ex.getMessage}")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}


  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
  }

  def deleteStudentFromDatabase(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя студента для удаления: ").trim
  if (name.isEmpty) return println("Имя не может быть пустым.")

  SQLiteDatabase.getAllStudents() match {
    case Success(all) =>
      if (!all.exists(_.name.equalsIgnoreCase(name)))
        return println("Студент не найден.")

      println(s"Вы уверены, что хотите удалить $name? (y/n)")
      val confirm = scala.io.StdIn.readLine().trim.toLowerCase
      if (confirm == "y") {
        deleteStudent(name)
        println(s"Студент $name удалён.")
      } else {
        println("Удаление отменено.")
      }

    case Failure(ex) =>
      println(s"Ошибка при подключении к базе данных: ${ex.getMessage}")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def insertProfessorToDatabase(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя преподавателя: ").trim
  if (name.isEmpty) return println("Имя не может быть пустым.")

  val title = scala.io.StdIn.readLine("Введите звание: ").trim
  if (title.isEmpty) return println("Звание не может быть пустым.")

  val email = scala.io.StdIn.readLine("Введите email: ").trim
  if (email.isEmpty) return println("Email не может быть пустым.")

  val disciplines = scala.io.StdIn.readLine("Введите дисциплины через запятую: ")
    .split(",").map(_.trim).filter(_.nonEmpty).toList
  if (disciplines.isEmpty) return println("Дисциплины не могут быть пустыми.")

  val faculty = chooseFaculty(faculties)
  val professor = Professor(name, title, email, disciplines, faculty)

  Try(insertProfessor(professor)) match {
    case Success(_) => println(s"Преподаватель $name добавлен.")
    case Failure(e) => println(s"Ошибка при добавлении: ${e.getMessage}")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def editProfessorByName(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя преподавателя для редактирования: ").trim

  SQLiteDatabase.getAllProfessors() match {
    case Some(all) =>
      all.find(_.name.equalsIgnoreCase(name)) match {
        case Some(professor) =>
          val newTitle = scala.io.StdIn.readLine(s"Введите новое звание (было ${professor.title}): ").trim
          val newEmail = scala.io.StdIn.readLine(s"Введите новый email (было ${professor.email}): ").trim
          val newDisciplines = scala.io.StdIn
            .readLine(s"Введите дисциплины через запятую (были: ${professor.disciplines.mkString(", ")}): ")
            .split(",").map(_.trim).filter(_.nonEmpty).toList
          val newFaculty = chooseFaculty(faculties)

          deleteProfessor(professor.name)
          insertProfessor(professor.copy(
            title = newTitle,
            email = newEmail,
            disciplines = newDisciplines,
            faculty = newFaculty
         @@ ))

          println("Данные обновлены.")
        case None =>
          println("Преподаватель не найден.")
      }

    case None =>
      println("Ошибка при получении данных из БД.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}


  def deleteProfessorFromDatabase(): Unit = {
    if (!isConnected) throw new NullPointerException("База данных недоступна")

    val name = scala.io.StdIn.readLine("Введите имя преподавателя для удаления: ").trim
    if (name.isEmpty) return println("Имя не может быть пустым.")

    val all = getAllProfessors()
    if (!all.exists(_.name.equalsIgnoreCase(name))) return println("Преподаватель не найден.")

    println(s"Вы уверены, что хотите удалить $name? (y/n)")
    val confirm = scala.io.StdIn.readLine().trim.toLowerCase
    if (confirm == "y") {
      deleteProfessor(name)
      println(s"Преподаватель $name удалён.")
    } else {
      println("Удаление отменено.")
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  def findProfessor(): Unit = {
    val faculty = chooseFaculty(faculties)
    val found = getAllProfessors().filter(_.faculty == faculty)

    if (found.isEmpty) println("Преподаватели не найдены.")
    else found.foreach(p => println(s"${p.name} - ${p.title}, Email: ${p.email}, Дисциплины: ${p.disciplines.mkString(", ")}"))

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  def showTimetable(): Unit = {
  val group = scala.io.StdIn.readLine("Введите название группы (например, 22-ИВТ-1): ").trim

  val timetable = SQLiteDatabase.getTimetableByGroup(group)

  if (timetable.nonEmpty) {
    println(s"\nРасписание для группы $group:")
    timetable.foreach(e =>
      println(s"${e.day}, ${e.time} — ${e.subject} (${e.room})")
    )
  } else {
    println("Расписание для этой группы не найдено.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
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
        println(s"\n✅ Расписание для группы $group сохранено.")
      } else {
        val day = daysOfWeek(choice - 1)
        val count = readInt(s"Сколько пар добавить на $day?", 1, 8)

        (1 to count).foreach { _ =>
          val time = scala.io.StdIn.readLine("Время пары (например, 08:00-09:35): ").trim
          val subject = scala.io.StdIn.readLine("Название предмета: ").trim
          val room = scala.io.StdIn.readLine("Аудитория: ").trim

          val entry = TimetableEntry(day, time, subject, room)
          SQLiteDatabase.insertTimetableEntry(entry, group)
        }

        loop() // рекурсивно продолжаем, пока не выберут завершение
      }
    }

    loop() // запуск рекурсивного цикла
    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  def deleteTimetableToDatabase(): Unit = {
  val group = scala.io.StdIn.readLine("Введите название группы для удаления расписания: ").trim
  val existing = SQLiteDatabase.getTimetableByGroup(group)

  if (existing.isEmpty) {
    println("Расписание для этой группы не найдено.")
  } else {
    println(s"Вы уверены, что хотите удалить расписание для группы $group? (y/n)")
    val confirm = scala.io.StdIn.readLine().trim.toLowerCase
    if (confirm == "y") {
      SQLiteDatabase.deleteTimetableByGroup(group)
      println("Расписание удалено.")
    } else {
      println("Операция отменена.")
    }
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  def editTimetable(): Unit = {
    val group = scala.io.StdIn.readLine("Введите название группы для редактирования: ").trim
    val existing = SQLiteDatabase.getTimetableByGroup(group)

    if (existing.isEmpty) {
      println("Расписание для этой группы не найдено.")
      println("\nНажмите Enter, чтобы вернуться в меню...")
      scala.io.StdIn.readLine()
      return
    }

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

        SQLiteDatabase.deleteTimetableByGroupAndDay(group, day)

        val count = readInt(s"Сколько пар хотите ввести на $day?", 0, 8)

        (1 to count).foreach { _ =>
          val time = scala.io.StdIn.readLine("Время пары (например, 08:00-09:35): ").trim
          val subject = scala.io.StdIn.readLine("Название предмета: ").trim
          val room = scala.io.StdIn.readLine("Аудитория: ").trim

          val entry = TimetableEntry(day, time, subject, room)
          SQLiteDatabase.insertTimetableEntry(entry, group)
        }

        println(s"День $day обновлён.")
        loop()
      }
    }

    loop()
    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }


  def showApplicantMenu(): Unit = {
    val score = readInt("Введите ваши баллы ЕГЭ: ", 0, 300)
    val available = directions.filter(d => score >= d.passScore)

    if (available.isEmpty) println("К сожалению, с такими баллами нет доступных направлений.")
    else {
      println("Вы можете поступить на следующие направления:")
      available.foreach(d => println(s"- ${d.name} (${d.faculty}), проходной балл: ${d.passScore}"))
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }  

  def studentActionsMenu(): Unit = {
  val options = Seq(
    ("1", "Добавить студента", () => insertStudentToDatabase()),
    ("2", "Отчислить студента", () => deleteStudentFromDatabase()),
    ("3", "Редактировать студента", () => editStudentByName()),
    ("4", "Назад", () => ()) // ничего не делает — выход из подменю
  )

  var back = false
  while (!back) {
    println("\n=== Действия над студентом ===")
    options.foreach { case (num, desc, _) => println(s"$num. $desc") }
    val choice = scala.io.StdIn.readLine("Выберите действие: ").trim
    options.find(_._1 == choice) match {
      case Some(("4", _, _)) => back = true
      case Some((_, _, action)) => action()
      case None => println("Неверный ввод.")
    }
  }
}

  def professorActionsMenu(): Unit = {
  val options = Seq(
    ("1", "Добавить преподавателя", () => insertProfessorToDatabase()),
    ("2", "Уволить преподавателя", () => deleteProfessorFromDatabase()),
    ("3", "Редактировать преподавателя", () => editProfessorByName()),
    ("4", "Назад", () => ()) 
  )

  var back = false
  while (!back) {
    println("\n=== Действия над преподавателем ===")
    options.foreach { case (num, desc, _) => println(s"$num. $desc") }
    val choice = scala.io.StdIn.readLine("Выберите действие: ").trim
    options.find(_._1 == choice) match {
      case Some(("4", _, _)) => back = true
      case Some((_, _, action)) => action()
      case None => println("Неверный ввод.")
    }
  }
}

  def timeTableActionsMenu(): Unit = {
  val options = Seq(
    ("1", "Добавить расписание", () => insertTimetableToDatabase()),
    ("2", "Убрать расписание", () => deleteTimetableToDatabase()),
    ("3", "Редактировать расписание", () => editTimetable()),
    ("4", "Назад", () => ()) 
  )

  var back = false
  while (!back) {
    println("\n=== Действия над расписанием ===")
    options.foreach { case (num, desc, _) => println(s"$num. $desc") }
    val choice = scala.io.StdIn.readLine("Выберите действие: ").trim
    options.find(_._1 == choice) match {
      case Some(("4", _, _)) => back = true
      case Some((_, _, action)) => action()
      case None => println("Неверный ввод.")
    }
  }
}

}
```


#### Short summary: 

empty definition using pc, found symbol in pc: copy.