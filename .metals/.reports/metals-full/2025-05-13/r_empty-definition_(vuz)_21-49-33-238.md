error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 331
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
text:
```scala
import Data._
import Models._

object Functions {
def findStudentByFacultyAndYear(faculty: String, year: Int): List[Student] = {
  students.filter(s => s.faculty == faculty && s.year == year)
}

def showStudentSearch(): Unit = {
  val faculty = scala.io.StdIn.readLine("Факультет: ")
  val year = scala.io.StdIn.readLine("Год посту@@пления: ").toInt
  val found = findStudentByFacultyAndYear(faculty, year)
  found.foreach(s => println(s"${s.name} — ${s.group}"))
}

def showTimetable(): Unit = {
    println("Здесь будет расписание (заглушка)")
}

def findProfessor(): Unit = {
    println("Здесь будет поиск преподавателя (заглушка)")
}
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.