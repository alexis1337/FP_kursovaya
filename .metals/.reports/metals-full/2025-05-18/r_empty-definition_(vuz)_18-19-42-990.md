error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/SQLiteDatabase.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/SQLiteDatabase.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 816
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/SQLiteDatabase.scala
text:
```scala
//> using dep "org.xerial:sqlite-jdbc:3.49.1.0"

import java.sql.{Connection, DriverManager, ResultSet}
import Models._

object SQLiteDatabase {
  val url = "jdbc:sqlite:university.db"

  lazy val connection: Connection = DriverManager.getConnection(url)

  def getAllStudents: List[Student] = {
    val stmt = connection.createStatement()
    val rs = stmt.executeQuery("SELECT * FROM students")
    Iterator.continually((rs, rs.next())).takeWhile(_._2).map { case (r, _) =>
      Student(
        name = r.getString("name"),
        group = r.getString("group_name"),
        year = r.getInt("year"),
        direction = r.getString("direction"),
        faculty = r.getString("faculty")
      )
    }.toList
  }

  def addStudent(student: Student): Unit = {
    val sql = "INSERT INTO students (name, group_name, @@year, direction, faculty) VALUES (?, ?, ?, ?, ?)"
    val stmt = connection.prepareStatement(sql)
    stmt.setString(1, student.name)
    stmt.setString(2, student.group)
    stmt.setInt(3, student.year)
    stmt.setString(4, student.direction)
    stmt.setString(5, student.faculty)
    stmt.executeUpdate()
    stmt.close()
  }

  def deleteStudentByName(name: String): Unit = {
    val sql = "DELETE FROM students WHERE name = ?"
    val stmt = connection.prepareStatement(sql)
    stmt.setString(1, name)
    stmt.executeUpdate()
    stmt.close()
  }

  def addProfessor(professor: Professor): Unit = {
    val sql = "INSERT INTO professors (name, title, email, disciplines, faculty) VALUES (?, ?, ?, ?, ?)"
    val stmt = connection.prepareStatement(sql)
    stmt.setString(1, professor.name)
    stmt.setString(2, professor.title)
    stmt.setString(3, professor.email)
    stmt.setString(4, professor.disciplines.mkString(","))
    stmt.setString(5, professor.faculty)
    stmt.executeUpdate()
    stmt.close()
  }

  def deleteProfessorByName(name: String): Unit = {
    val sql = "DELETE FROM professors WHERE name = ?"
    val stmt = connection.prepareStatement(sql)
    stmt.setString(1, name)
    stmt.executeUpdate()
    stmt.close()
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.