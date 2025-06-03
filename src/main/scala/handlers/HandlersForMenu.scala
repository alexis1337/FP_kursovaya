import com.sun.net.httpserver.HttpExchange
import cats.effect.IO
import HttpUtils._
import Jokes._ 

object HandlersForMenu {

  // Обработчик страницы меню
  def menuHandler(exchange: HttpExchange): IO[Unit] = {
    for {
      template <- readResourceFile("menu.html")
      joke <- getRandomJoke 
      pageWithJoke = s"$template\n<footer><small style=\"color:gray; font-size:small;\">$joke</small></footer>"
      _ <- sendResponse(exchange, pageWithJoke)
    } yield ()
  }.handleErrorWith { e =>
    val errorHtml = s"<html><body><h1>Ошибка: ${e.getMessage}</h1></body></html>"
    sendResponse(exchange, errorHtml, 500)
  }

  // Редирект с корня "/" на "/menu"
  def redirectToMenuHandler(exchange: HttpExchange): IO[Unit] = IO {
    val headers = exchange.getResponseHeaders
    headers.add("Location", "/menu")
    exchange.sendResponseHeaders(302, -1)
    exchange.close()
  }
}
