error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Models.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Models.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 629
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Models.scala
text:
```scala
object Models {
  case class Student(name: String, group: String, year: Int, direction: String, faculty: String)
  case class Professor(name: String, title: String, email: String, disciplines: List[String], faculty: String)
  case class User(username: String, password: String, role: Role)
  case class TimetableEntry(day: String, time: String, subject: String, room: String)
  case class GroupTimetable(group: String, entries: List[TimetableEntry])
  case class Direction(name: String, faculty: String, passScore: Int) // Новая модель

  sealed trait Role
  case object StudentRole extends Role
  case object ProfessorRole exten@@ds Role
}


```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.