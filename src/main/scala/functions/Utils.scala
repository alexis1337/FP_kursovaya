import scala.util.{Try, Success, Failure}
import cats.implicits._
import cats.effect.IO

import SQLiteDatabase._

object Utils {

    // Оборачиваем ввод и вывод
  def printLine(msg: String): IO[Unit] = IO(println(msg))

  def readLine(prompt: String): IO[String] = IO {
    print(prompt)
    scala.io.StdIn.readLine()
  }

  // Чтение целого числа с проверкой границ
  def readInt(prompt: String, min: Int = Int.MinValue, max: Int = Int.MaxValue): IO[Int] = {
    def loop(): IO[Int] = for {
      _ <- printLine(prompt)
      input <- readLine("")
      result = input.toIntOption.filter(n => n >= min && n <= max)
      res <- result match {
        case Some(value) => IO.pure(value)
        case None => printLine(s"Ошибка! Введите число от $min до $max.") >> loop()
      }
    } yield res

    loop()
  }

  // Выбор факультета
  def chooseFaculty(faculties: List[String]): IO[String] = {
    def loop(): IO[String] = for {
      _ <- faculties.zipWithIndex.traverse { case (facultyName, index) =>
      printLine(s"${index + 1}. $facultyName")
      }
      index <- readInt("Введите номер факультета: ", 1, faculties.length)
    } yield faculties(index - 1)
    loop()
  }

  // Проверка соединения с БД 
  def isConnected: Boolean = {
    Try(getAllStudents()).isSuccess
  }

  // Служебная функция валидации (в том числе и для списка)
  trait EmptyCheckable[A] {
    def isEmpty(a: A): Boolean
  }

  given EmptyCheckable[String] with 
    def isEmpty(a: String): Boolean = a.trim.isEmpty

  given [T]: EmptyCheckable[List[T]] with
    def isEmpty(a: List[T]): Boolean = a.isEmpty

  def validateNonEmptyGeneric[A](field: A, fieldName: String)(using checker: EmptyCheckable[A]): Either[String, A] = {
    if checker.isEmpty(field) then Left(s"$fieldName не может быть пустым.")
    else Right(field)
  }
}