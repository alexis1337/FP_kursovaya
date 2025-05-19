error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/StudentFunctions.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/StudentFunctions.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -chooseFaculty.
	 -chooseFaculty#
	 -chooseFaculty().
	 -scala/Predef.chooseFaculty.
	 -scala/Predef.chooseFaculty#
	 -scala/Predef.chooseFaculty().
offset: 80
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/StudentFunctions.scala
text:
```scala
package functions



def showStudentSearch(): Unit = {
  val faculty = chooseFac@@ulty(faculties)
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

  def createStudent(name: String, group: String, year: Int, direction: String, faculty: String): Either[String, Student] = {
    if (name.isEmpty) Left("Имя не может быть пустым.")
    else if (group.isEmpty) Left("Группа не может быть пустой.")
    else if (direction.isEmpty) Left("Направление не может быть пустым.")
    else Right(Student(name, group, year, direction, faculty))
  }

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

  def updateStudent(original: Student, newGroup: String, newYear: Int, newDirection: String, newFaculty: String): Either [String, Student] ={
  if (newGroup.isEmpty) Left("Группа не может быть пустой.")
  else if (newDirection.isEmpty) Left("Направление не может быть пустым.")
  else if (newFaculty.isEmpty) Left("Факультет не может быть пустым.")
  else Right(original.copy(group = newGroup, year = newYear, direction = newDirection, faculty = newFaculty))
}

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

  def removeStudent(name: String, confirmInput: String): Boolean =
  confirmInput.trim.toLowerCase == "y" && name.nonEmpty

  def deleteStudentFromDatabase(): Unit = {
  val name = scala.io.StdIn.readLine("Введите имя студента для удаления: ").trim

  if (name.isEmpty) return println("Имя не может быть пустым.")

  SQLiteDatabase.getAllStudents() match {
    case Success(all) =>
      if (!all.exists(_.name.equalsIgnoreCase(name))) return println("Студент не найден.")

      println(s"Вы уверены, что хотите удалить $name? (y/n)")
      val confirm = scala.io.StdIn.readLine()

      if (removeStudent(name, confirm)) {
        deleteStudent(name)
        println(s"Студент $name удалён.")
      } else {
        println("Удаление отменено.")
      }
    case Failure(e) =>
      println(s"Ошибка при доступе к БД: ${e.getMessage}")
  }

  println("\nНажмите Enter, чтобы вернуться в меню...")
  scala.io.StdIn.readLine()
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.