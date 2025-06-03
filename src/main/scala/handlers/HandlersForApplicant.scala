import com.sun.net.httpserver.HttpExchange
import java.nio.charset.StandardCharsets
import java.net.URLDecoder
import java.io.OutputStream
import scala.util.Try
import cats.effect.IO
import cats.implicits._

import Models._
import Data._
import SQLiteDatabase._
import HttpUtils._  

object HandlersForApplicant {

    def applicantHandler(exchange: HttpExchange): IO[Unit] = {
    val method = exchange.getRequestMethod

    def handleGet: IO[Unit] =
      for {
        template <- readResourceFile("applicant.html")
        page = template.replace("{resultSection}", "")
        _ <- sendResponse(exchange, page)
      } yield ()

    def handlePost: IO[Unit] = for {
      body <- IO(scala.io.Source.fromInputStream(exchange.getRequestBody).mkString)
      params = parseForm(body)
      scoreStr = params.getOrElse("score", "")
      scoreOpt = scoreStr.toIntOption
      resultHtml = scoreOpt match {
      case Some(score) if score >= 0 && score <= 300 =>  val available = directions.filter(d => score >= d.passScore)
        if (available.isEmpty) "<p>К сожалению, с такими баллами нет доступных направлений.</p>"
        else {
          val listItems = available.map(d => s"<li>${d.name} (${d.faculty}), проходной балл: ${d.passScore}</li>").mkString("\n")
          s"<p>Вы можете поступить на следующие направления:</p><ul>$listItems</ul>"
        }
      case _ => "<p>Некорректный ввод баллов ЕГЭ.</p>"
      }
      template <- readResourceFile("applicant.html")
      page = template.replace("{resultSection}", resultHtml)
      _ <- sendResponse(exchange, page)
    } yield ()

    def handleUnsupported: IO[Unit] = IO {
      exchange.sendResponseHeaders(405, -1)
      exchange.close()
    }

    // Выбираем ветку по методу
    method match {
      case "GET" => handleGet.handleErrorWith(e => sendResponse(exchange, s"<html><body><h1>Error: ${e.getMessage}</h1></body></html>"))
      case "POST" => handlePost.handleErrorWith(e => sendResponse(exchange, s"<html><body><h1>Error: ${e.getMessage}</h1></body></html>"))
      case _ => handleUnsupported
    }
  }
}