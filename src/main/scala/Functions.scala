import Data._
import Models._
import SQLiteDatabase._
import scala.util.{Try, Success, Failure}

object Functions {

                                               // Утилиты

  // Чтение целого числа с проверкой границ
  def readInt(prompt: String, min: Int = Int.MinValue, max: Int = Int.MaxValue): Int = {
  Iterator.continually {
    println(prompt)
    val input = scala.io.StdIn.readLine()
    input.toIntOption match {
      case Some(num) if num >= min && num <= max => Some(num)
      case _ =>
        println(s"Ошибка! Введите число от $min до $max.")
        None
    }
  }.collectFirst { case Some(value) => value }.getOrElse {
  throw new RuntimeException("Не удалось прочитать корректное число.")
    }
  }

  // Выбор факультета из списка
  def chooseFaculty(faculties: List[String]): String = {
    Iterator.continually {
      faculties.zipWithIndex.foreach { case (facultyName, index) => println(s"${index + 1}. $facultyName") }
      val index = readInt("Введите номер факультета: ", 1, faculties.length)
      faculties(index - 1)
    }.next()
  }

  // Проверка соединения с БД 
  def isConnected: Boolean = {
    Try(getAllStudents()).isSuccess
  }

                                          // Работа со студентами

  // Поиск студента по факультету и году поступления
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

  // Создание экземпляра с валидацией
  def createStudent(name: String, group: String, year: Int, direction: String, faculty: String): Either[String, Student] = {
    if (name.isEmpty) Left("Имя не может быть пустым.")
    else if (group.isEmpty) Left("Группа не может быть пустой.")
    else if (direction.isEmpty) Left("Направление не может быть пустым.")
    else Right(Student(name, group, year, direction, faculty))
  }

  // Добавление в БД через ввод
  def insertStudentToDatabase(): Unit = {
    val name = scala.io.StdIn.readLine("Введите имя студента: ").trim
    val group = scala.io.StdIn.readLine("Введите группу: ").trim
    val year = readInt("Введите год поступления: ", 2021, 2024)
    val direction = scala.io.StdIn.readLine("Введите направление: ").trim
    val faculty = chooseFaculty(faculties)

    createStudent(name, group, year, direction, faculty) match {
      case Right(student) =>
        Try(insertStudent(student)) match {
          case Success(_) => println(s"Студент $name добавлен в базу данных.")
          case Failure(e) => println(s"Ошибка при добавлении: ${e.getMessage}")
        }
      case Left(errorMsg) =>
        println(s"Ошибка: $errorMsg")
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  // Новая версия с изменёнными полями  
  def updateStudent(original: Student, newGroup: String, newYear: Int, newDirection: String, newFaculty: String): Either [String, Student] ={
  if (newGroup.isEmpty) Left("Группа не может быть пустой.")
  else if (newDirection.isEmpty) Left("Направление не может быть пустым.")
  else if (newFaculty.isEmpty) Left("Факультет не может быть пустым.")
  else Right(original.copy(group = newGroup, year = newYear, direction = newDirection, faculty = newFaculty))
}

  // Редактирование
  def editStudentByName(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя студента для редактирования: ").trim

  SQLiteDatabase.getAllStudents() match {
    case Success(all) =>
      all.find(_.name.equalsIgnoreCase(name)) match {
        case Some(student) =>
          val newGroup = scala.io.StdIn.readLine(s"Новая группа (было ${student.group}): ").trim
          val newYear = readInt(s"Новый год поступления (было ${student.year}): ", 2021, 2024)
          val newDirection = scala.io.StdIn.readLine(s"Новое направление (было ${student.direction}): ").trim
          val newFaculty = chooseFaculty(faculties)

          updateStudent(student, newGroup, newYear, newDirection, newFaculty) match {
            case Right(updatedStudent) =>
              deleteStudent(student.name)
              insertStudent(updatedStudent)
              println("Данные обновлены.")
            case Left(errorMessage) =>
              println(s"Ошибка: $errorMessage")
          }

        case None => println("Студент не найден.")
      }
    case Failure(e) => println(s"Ошибка БД: ${e.getMessage}")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

  // Подтверждение удаления
  def removeStudent(name: String, confirmInput: String): Boolean =
  confirmInput.trim.toLowerCase == "y" && name.nonEmpty

  // Удаление студента из списка
  def deleteStudentFromDatabase(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя студента для отчисления: ").trim

  if (name.isEmpty) return println("Имя не может быть пустым.")

  SQLiteDatabase.getAllStudents() match {
    case Success(all) =>
      if (!all.exists(_.name.equalsIgnoreCase(name))) return println("Студент не найден.")

      println(s"Вы уверены, что хотите отчислить $name? (y/n)")
      val confirm = scala.io.StdIn.readLine()

      if (removeStudent(name, confirm)) {
        deleteStudent(name)
        println(s"Студент $name отчислен.")
      } else {
        println("Операция отменена.")
      }
    case Failure(e) =>
      println(s"Ошибка при доступе к БД: ${e.getMessage}")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

                                          // Работа с преподавателями

  def findProfessor(): Unit = {
    val faculty = chooseFaculty(faculties)

    SQLiteDatabase.getAllProfessors() match {
      case Some(professors) =>
        val found = professors.filter(_.faculty == faculty)
        if (found.isEmpty)
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
  val name = scala.io.StdIn.readLine("Введите имя преподавателя для увольнения: ").trim
  if (name.isEmpty) return println("Имя не может быть пустым.")

  SQLiteDatabase.getAllProfessors() match {
    case Some(all) =>
      if (!all.exists(_.name.equalsIgnoreCase(name)))
        return println("Преподаватель не найден.")

      println(s"Вы уверены, что хотите уволить $name? (y/n)")
      val confirm = scala.io.StdIn.readLine()

      if (removeProfessor(name, confirm)) {
        deleteProfessor(name)
        println(s"Преподаватель $name уволен.")
      } else {
        println("Операция отменена.")
      }

    case None =>
      println("Ошибка подключения к базе данных.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

                                          // Работа с расписанием

  def showTimetable(): Unit = {
  val group = scala.io.StdIn.readLine("Введите название группы (например, 22-ИВТ-3): ").trim

  SQLiteDatabase.getTimetableByGroup(group) match {
    case Some(entries) if entries.nonEmpty =>
      println(s"\nРасписание для группы $group:")
      entries.foreach { e =>
        println(s"${e.day}, ${e.time} — ${e.subject} (${e.room})")
      }
    case Some(_) =>
      println("Расписание для этой группы пустое.")
    case None =>
      println("Группа не найдена.")
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
                    case Success(_) => 
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
        println("Группа не найдена.")
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }

  def deleteTimetableToDatabase(): Unit = {
  val group = scala.io.StdIn.readLine("Введите группу: ").trim

  SQLiteDatabase.getTimetableByGroup(group) match {
    case Some(entries) if entries.nonEmpty =>
      println(s"Хотите удалить расписание для группы $group? (y/n)")
      val confirm = scala.io.StdIn.readLine().trim.toLowerCase
      if (confirm == "y") {
        SQLiteDatabase.deleteTimetableByGroup(group)
        println("Расписание удалено.")
      } else println("Операция отменена.")
    case Some(_) =>
      println("Расписание уже пустое.")
    case None =>
      println("Группа не найдена.")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

                                    // Меню для абитуриентов (тестовая версия)
 
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

                                                // Подменю

  def studentActionsMenu(): Unit = {
  val options = Seq(
    ("1", "Добавить студента", () => insertStudentToDatabase()),
    ("2", "Отчислить студента", () => deleteStudentFromDatabase()),
    ("3", "Редактировать студента", () => editStudentByName()),
    ("4", "Назад", () => ()) 
  )

  def loop(): Unit = {
    println("\n=== Действия над студентом ===")
    options.foreach { case (num, desc, _) => println(s"$num. $desc") }
    val choice = scala.io.StdIn.readLine("Выберите действие: ").trim

    options.find(_._1 == choice) match {
      case Some(("4", _, _)) => () 
      case Some((_, _, action)) =>
        action()
        loop() 
      case None =>
        println("Неверный ввод.")
        loop() 
    }
  }

  loop()
}

  def professorActionsMenu(): Unit = {
  val options = Seq(
    ("1", "Добавить преподавателя", () => insertProfessorToDatabase()),
    ("2", "Уволить преподавателя", () => deleteProfessorFromDatabase()),
    ("3", "Редактировать преподавателя", () => editProfessorByName()),
    ("4", "Назад", () => ())
  )

  def loop(): Unit = {
    println("\n=== Действия над преподавателем ===")
    options.foreach { case (num, desc, _) => println(s"$num. $desc") }
    val choice = scala.io.StdIn.readLine("Выберите действие: ").trim

    options.find(_._1 == choice) match {
      case Some(("4", _, _)) => ()
      case Some((_, _, action)) =>
        action()
        loop()
      case None =>
        println("Неверный ввод.")
        loop()
    }
  }

  loop()
}

  def timeTableActionsMenu(): Unit = {
  val options = Seq(
    ("1", "Добавить расписание", () => insertTimetableToDatabase()),
    ("2", "Удалить расписание", () => deleteTimetableToDatabase()),
    ("3", "Редактировать расписание", () => editTimetable()),
    ("4", "Назад", () => ())
  )

  def loop(): Unit = {
    println("\n=== Действия над расписанием ===")
    options.foreach { case (num, desc, _) => println(s"$num. $desc") }
    val choice = scala.io.StdIn.readLine("Выберите действие: ").trim

    options.find(_._1 == choice) match {
      case Some(("4", _, _)) => ()
      case Some((_, _, action)) =>
        action()
        loop()
      case None =>
        println("Неверный ввод.") 
        loop()
    }
  }

  loop()
}
}