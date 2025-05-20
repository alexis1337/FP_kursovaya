error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/SQLiteDatabase.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/SQLiteDatabase.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 533
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/SQLiteDatabase.scala
text:
```scala
//> using dep "org.xerial:sqlite-jdbc:3.49.1.0"

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import Models._
import scala.util.{Try, Success, Failure}

object SQLiteDatabase {

  // Соединение с БД
  private val connection: Try[Connection] = Try {
  DriverManager.getConnection("jdbc:sqlite:university.db")
}

  // Создание таблицы со студентами
  def createStudentsTable(): Unit = {
  connection match {
    case Success(conn) =>
      val stmt = conn.createStatement()
      val sql = """
C@@REATE TABLE IF NOT EXISTS students (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  group_name TEXT NOT NULL,
  year INTEGER NOT NULL,
  direction TEXT NOT NULL,
  faculty TEXT NOT NULL
)
""".stripMargin
      stmt.execute(sql)
      stmt.close()
    case Failure(ex) =>
      println(s"Ошибка при подключении к базе данных: ${ex.getMessage}")
  }
}

  // Создание таблицы с преподавателями
  def createProfessorsTable(): Unit = {
  connection match {
    case Success(conn) =>
      val stmt = conn.createStatement()
      val sql = """
    CREATE TABLE IF NOT EXISTS professors (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      title TEXT NOT NULL,
      email TEXT NOT NULL,
      disciplines TEXT NOT NULL,
      faculty TEXT NOT NULL
    )
  """.stripMargin
      stmt.execute(sql)
      stmt.close()
    case Failure(ex) =>
      println(s"Ошибка при подключении к базе данных: ${ex.getMessage}")
  }
}

  // Создание таблицы с расписанием
  def createTimetableTable(): Unit = {
  connection match {
    case Success(conn) =>
      val stmt = conn.createStatement()
      val sql = """
CREATE TABLE IF NOT EXISTS timetables (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  group_name TEXT NOT NULL,
  day TEXT NOT NULL,
  time TEXT NOT NULL,
  subject TEXT NOT NULL,
  room TEXT NOT NULL
)
""".stripMargin
      stmt.execute(sql)
      stmt.close()
    case Failure(ex) =>
      println(s"Ошибка при подключении к базе данных: ${ex.getMessage}")
  }
}

  // Вставка нового студента в БД
  def insertStudent(student: Student): Unit = {
  connection.toOption.foreach { conn =>
    val insertSQL =
      "INSERT INTO students (name, group_name, year, direction, faculty) VALUES (?, ?, ?, ?, ?)"
    val pstmt = conn.prepareStatement(insertSQL)
    pstmt.setString(1, student.name)
    pstmt.setString(2, student.group)
    pstmt.setInt(3, student.year)
    pstmt.setString(4, student.direction)
    pstmt.setString(5, student.faculty)
    pstmt.executeUpdate()
    pstmt.close()
  }
}

  // Удаление студента из БД
  def deleteStudent(name: String): Unit = {
  connection.toOption.foreach { conn =>
    val sql = "DELETE FROM students WHERE name = ?"
    val stmt = conn.prepareStatement(sql)
    stmt.setString(1, name)
    stmt.executeUpdate()
    stmt.close()
  }
}

  // Получение всех студентов из БД
  def getAllStudents(): Try[List[Student]] = connection.flatMap { conn =>
  Try {
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery("SELECT * FROM students")
    val students = Iterator
      .continually(rs)
      .takeWhile(_.next())
      .map(rs =>
        Student(
          rs.getString("name"),
          rs.getString("group_name"),
          rs.getInt("year"),
          rs.getString("direction"),
          rs.getString("faculty")
        )
      )
      .toList
    rs.close()
    stmt.close()
    students
  }
}

  // Вставка нового преподавателя в БД
  def insertProfessor(professor: Professor): Unit = {
  connection.toOption.foreach { conn =>
    val sql = "INSERT INTO professors (name, title, email, disciplines, faculty) VALUES (?, ?, ?, ?, ?)"
    val stmt = conn.prepareStatement(sql)
    stmt.setString(1, professor.name)
    stmt.setString(2, professor.title)
    stmt.setString(3, professor.email)
    stmt.setString(4, professor.disciplines.mkString(","))
    stmt.setString(5, professor.faculty)
    stmt.executeUpdate()
    stmt.close()
  }
}

  // Удаление преподавателя из БД
  def deleteProfessor(name: String): Unit = {
  connection.toOption.foreach { conn =>
    val sql = "DELETE FROM professors WHERE name = ?"
    val stmt = conn.prepareStatement(sql)
    stmt.setString(1, name)
    stmt.executeUpdate()
    stmt.close()
  }
}

  // Получение всех преподавателей из БД
  def getAllProfessors(): Option[List[Professor]] = {
  connection.toOption.map { conn =>
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery("SELECT * FROM professors")

    val professors = Iterator
      .continually(rs)
      .takeWhile(_.next())
      .map { rs =>
        val disciplines = rs.getString("disciplines").split(",").map(_.trim).toList
        Professor(
          rs.getString("name"),
          rs.getString("title"),
          rs.getString("email"),
          disciplines,
          rs.getString("faculty")
        )
      }
      .toList

    rs.close()
    stmt.close()
    professors
  }
}

  // Вставка нового расписания в БД
  def insertTimetableEntry(entry: TimetableEntry, group: String): Unit = {
  connection.toOption.foreach { conn =>
    val sql = "INSERT INTO timetables (group_name, day, time, subject, room) VALUES (?, ?, ?, ?, ?)"
    val pstmt = conn.prepareStatement(sql)
    pstmt.setString(1, group)
    pstmt.setString(2, entry.day)
    pstmt.setString(3, entry.time)
    pstmt.setString(4, entry.subject)
    pstmt.setString(5, entry.room)
    pstmt.executeUpdate()
    pstmt.close()
  }
}

  // Удаление расписания из БД
  def deleteTimetableByGroup(group: String): Unit = {
  connection.toOption.foreach { conn =>
    val sql = "DELETE FROM timetables WHERE group_name = ?"
    val stmt = conn.prepareStatement(sql)
    stmt.setString(1, group)
    stmt.executeUpdate()
    stmt.close()
  }
}

  // Получение расписания по группе
  def getTimetableByGroup(group: String): Option[List[TimetableEntry]] = {
  connection.toOption.map { conn =>
    val sql = "SELECT day, time, subject, room FROM timetables WHERE group_name = ?"
    val stmt = conn.prepareStatement(sql)
    stmt.setString(1, group)
    val rs = stmt.executeQuery()

    val entries = Iterator
      .continually(rs)
      .takeWhile(_.next())
      .map(rs =>
        TimetableEntry(
          rs.getString("day"),
          rs.getString("time"),
          rs.getString("subject"),
          rs.getString("room")
        )
      )
      .toList

    rs.close()
    stmt.close()
    entries
  }
}

  // Удаление дня с расписанием 
  def deleteTimetableByGroupAndDay(group: String, day: String): Unit = {
  connection.toOption.foreach { conn =>
    val sql = "DELETE FROM timetables WHERE group_name = ? AND day = ?"
    val stmt = conn.prepareStatement(sql)
    stmt.setString(1, group)
    stmt.setString(2, day)
    stmt.executeUpdate()
    stmt.close()
  }
}

  // Закрытие соединения с БД
  def closeConnection(): Unit = {
  connection.toOption.foreach { conn =>
    if (!conn.isClosed) conn.close()
  }
}
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.