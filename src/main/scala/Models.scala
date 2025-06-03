object Models { // Предполагается внедрение аутентификации
  case class Student(name: String, group: String, year: Int, direction: String, faculty: String)
  case class Professor(name: String, title: String, email: String, disciplines: List[String], faculty: String)
  case class User(username: String, password: String, role: Role)
  case class TimetableEntry(day: String, time: String, subject: String, room: String)
  case class GroupTimetable(group: String, entries: List[TimetableEntry])
  case class Direction(name: String, faculty: String, passScore: Int)

  case class MenuState(selectedOption: String)

  sealed trait Role
  case object StudentRole extends Role
  case object ProfessorRole extends Role
  case object DecanatRole extends Role
}