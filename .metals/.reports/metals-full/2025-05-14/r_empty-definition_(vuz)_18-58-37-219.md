error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1155
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
text:
```scala
import Data._
import Models._

object Functions {

  def findStudentByFacultyAndYear(faculty: String, year: Int): List[Student] = {
    val foundStudents = students.filter { s =>
      val matchFaculty = s.faculty.equalsIgnoreCase(faculty)
      val matchYear = s.year == year
      matchFaculty && matchYear
    }
    foundStudents
  }

  def chooseFaculty(faculties: List[String]): String = {
    println("Выберите институт:")
    faculties.zipWithIndex.foreach { case (faculty, index) =>
      println(s"${index + 1}. $faculty")
    }
    val choice = scala.io.StdIn.readLine("Введите номер: ").toIntOption.getOrElse(0)
    faculties.lift(choice - 1).getOrElse {
      println("Неверный ввод. Повторите попытку.")
      chooseFaculty(faculties)
    }
  }

  def showStudentSearch(): Unit = {
    val faculty = chooseFaculty(faculties)
    val year = scala.io.StdIn.readLine("Год поступления: ").toInt

    val found = findStudentByFacultyAndYear(faculty, year)

    if found.isEmpty then println("Студенты не найдены.")
    else found.foreach(s => println(s"${s.name} — ${s.group} (${s.direction})"))
  }

  def showTimetable(): Unit = {
    println("З@@десь будет расписание (заглушка)")
  }

  def findProfessor(): Unit = {
    println("Здесь будет поиск преподавателя (заглушка)")
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.