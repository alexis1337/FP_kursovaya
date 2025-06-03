import cats.effect._
import cats.implicits._

import Data._
import Models._
import Utils._ 

object Applicant {
  def showApplicantMenu(): IO[Unit] = for {
      scoreStr <- readLine("Введите ваши баллы ЕГЭ: ")
      score = scoreStr.toIntOption.getOrElse(-1)
      available = if (score >= 0) directions.filter(d => score >= d.passScore) else List.empty
      _ <- if (score < 0)
        printLine("Некорректный ввод баллов ЕГЭ.")
      else if (available.isEmpty)
        printLine("К сожалению, с такими баллами нет доступных направлений.")
      else for {
        _ <- printLine("Вы можете поступить на следующие направления:")
        _ <- available.traverse_(d => printLine(s"- ${d.name} (${d.faculty}), проходной балл: ${d.passScore}"))
      } yield ()
      _ <- printLine("\nНажмите Enter, чтобы вернуться в меню...")
      _ <- readLine("")
    } yield () 
  }