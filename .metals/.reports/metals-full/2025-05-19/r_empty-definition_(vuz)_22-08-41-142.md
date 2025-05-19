error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/models/Models.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/models/Models.scala
empty definition using pc, found symbol in pc: `<none>`.
semanticdb not found
empty definition using fallback
non-local guesses:
	 -GroupTimetable#
	 -scala/Predef.GroupTimetable#
offset: 413
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/models/Models.scala
text:
```scala
package models

object Models {
  case class Student(name: String, group: String, year: Int, direction: String, faculty: String)
  case class Professor(name: String, title: String, email: String, disciplines: List[String], faculty: String)
  case class User(username: String, password: String, role: Role)
  case class TimetableEntry(day: String, time: String, subject: String, room: String)
  case class GroupTim@@etable(group: String, entries: List[TimetableEntry])
  case class Direction(name: String, faculty: String, passScore: Int)

  sealed trait Role
  case object StudentRole extends Role
  case object ProfessorRole extends Role
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.