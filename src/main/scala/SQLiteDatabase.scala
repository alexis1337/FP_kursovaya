//> using dep "org.xerial:sqlite-jdbc:3.49.1.0"

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import scala.util.{Try, Success, Failure}
import Models._

object SQLiteDatabase {
  // Открытие соединения 
  private def withConnection[A](f: Connection => A): Try[A] = Try {
    val conn = DriverManager.getConnection("jdbc:sqlite:university.db")
    try f(conn)
    finally conn.close()
  }

  // Вспомогательный метод для запросов на изменение данных
  private def executeUpdate(sql: String)(setParams: PreparedStatement => Unit): Try[Int] =
    withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      setParams(ps)
      val count = ps.executeUpdate()
      ps.close()
      count
    }

  // Вспомогательный метод для выборок списка
  private def queryList[A](sql: String, paramSetter: Option[PreparedStatement => Unit] = None)
                          (mapper: ResultSet => A): Try[List[A]] =
    withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      paramSetter.foreach(_(ps))
      val rs = ps.executeQuery()
      val buf = Iterator.continually(rs).takeWhile(_.next()).map(mapper).toList
      rs.close()
      ps.close()
      buf
    }

  // Создание таблиц
  def createStudentsTable(): Try[Unit] = withConnection { conn =>
    val sql = """
      |CREATE TABLE IF NOT EXISTS students (
      |  id INTEGER PRIMARY KEY AUTOINCREMENT,
      |  name TEXT NOT NULL,
      |  group_name TEXT NOT NULL,
      |  year INTEGER NOT NULL,
      |  direction TEXT NOT NULL,
      |  faculty TEXT NOT NULL
      |)
    """.stripMargin
    conn.createStatement().execute(sql)
  }

  def createProfessorsTable(): Try[Unit] = withConnection { conn =>
    val sql = """
      |CREATE TABLE IF NOT EXISTS professors (
      |  id INTEGER PRIMARY KEY AUTOINCREMENT,
      |  name TEXT NOT NULL,
      |  title TEXT NOT NULL,
      |  email TEXT NOT NULL,
      |  disciplines TEXT NOT NULL,
      |  faculty TEXT NOT NULL
      |)
    """.stripMargin
    conn.createStatement().execute(sql)
  }

  def createTimetableTable(): Try[Unit] = withConnection { conn =>
    val sql = """
      |CREATE TABLE IF NOT EXISTS timetables (
      |  id INTEGER PRIMARY KEY AUTOINCREMENT,
      |  group_name TEXT NOT NULL,
      |  day TEXT NOT NULL,
      |  time TEXT NOT NULL,
      |  subject TEXT NOT NULL,
      |  room TEXT NOT NULL
      |)
    """.stripMargin
    conn.createStatement().execute(sql)
  }

  // CRUD для студентов
  def insertStudent(student: Student): Try[Int] = executeUpdate(
    "INSERT INTO students (name, group_name, year, direction, faculty) VALUES (?, ?, ?, ?, ?)"
  ) { ps =>
    println(s"Inserting student: $student")
    ps.setString(1, student.name)
    ps.setString(2, student.group)
    ps.setInt(3, student.year)
    ps.setString(4, student.direction)
    ps.setString(5, student.faculty)
  }

  def deleteStudent(name: String): Try[Int] = executeUpdate(
    "DELETE FROM students WHERE name = ?"
  )(_.setString(1, name))

  def getAllStudents(): Try[List[Student]] = queryList(
    "SELECT name, group_name, year, direction, faculty FROM students"
  )(rs => {
    val student = Student(
      rs.getString("name"),
      rs.getString("group_name"),
      rs.getInt("year"),
      rs.getString("direction"),
      rs.getString("faculty")
    )
    println(s"Read student: $student")
    student
  })

  // CRUD для преподавателей
  def insertProfessor(prof: Professor): Try[Int] = executeUpdate(
    "INSERT INTO professors (name, title, email, disciplines, faculty) VALUES (?, ?, ?, ?, ?)"
  ) { ps =>
    ps.setString(1, prof.name)
    ps.setString(2, prof.title)
    ps.setString(3, prof.email)
    ps.setString(4, prof.disciplines.mkString(","))
    ps.setString(5, prof.faculty)
  }

  def deleteProfessor(name: String): Try[Int] = executeUpdate(
    "DELETE FROM professors WHERE name = ?"
  )(_.setString(1, name))

  def getAllProfessors(): Try[List[Professor]] = queryList(
    "SELECT name, title, email, disciplines, faculty FROM professors"
  )(rs => {
    val discs = rs.getString("disciplines").split(",").map(_.trim).toList
    Professor(
      rs.getString("name"),
      rs.getString("title"),
      rs.getString("email"),
      discs,
      rs.getString("faculty")
    )
  })

  // CRUD для расписания
  def insertTimetableEntry(entry: TimetableEntry, group: String): Try[Int] = executeUpdate(
    "INSERT INTO timetables (group_name, day, time, subject, room) VALUES (?, ?, ?, ?, ?)"
  ) { ps =>
    ps.setString(1, group)
    ps.setString(2, entry.day)
    ps.setString(3, entry.time)
    ps.setString(4, entry.subject)
    ps.setString(5, entry.room)
  }

  def deleteTimetableByGroup(group: String): Try[Int] = executeUpdate(
    "DELETE FROM timetables WHERE group_name = ?"
  )(_.setString(1, group))

  def deleteTimetableByGroupAndDay(group: String, day: String): Try[Int] = executeUpdate(
    "DELETE FROM timetables WHERE group_name = ? AND day = ?"
  ) { ps =>
    ps.setString(1, group)
    ps.setString(2, day)
  }

  def getTimetableByGroup(group: String): Try[List[TimetableEntry]] = queryList(
    "SELECT day, time, subject, room FROM timetables WHERE group_name = ?",
    Some(_.setString(1, group))
  )(rs => TimetableEntry(
    rs.getString("day"),
    rs.getString("time"),
    rs.getString("subject"),
    rs.getString("room")
  ))
}