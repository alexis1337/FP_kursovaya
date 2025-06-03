import com.sun.net.httpserver.HttpExchange
import java.nio.charset.StandardCharsets
import java.net.URLDecoder
import java.io.OutputStream
import scala.io.Source
import cats.effect.IO

object HttpUtils {

  // Отправка HTTP-ответа 
  def sendResponse(exchange: HttpExchange, content: String, status: Int = 200): IO[Unit] = IO {
    val bytes = content.getBytes(StandardCharsets.UTF_8)
    exchange.getResponseHeaders.add("Content-Type", "text/html; charset=utf-8")
    exchange.sendResponseHeaders(status, bytes.length)
    val os: OutputStream = exchange.getResponseBody
    os.write(bytes)
    os.close()
  }

  // Чтение исходного файла 
  def readResourceFile(path: String): IO[String] = IO {
    val stream = getClass.getResourceAsStream(s"/$path")
    if (stream == null) throw new Exception(s"Ресурс не найден: $path")
    try {
      Source.fromInputStream(stream).mkString
    } finally {
      stream.close()
    }
  }

  /**
   * Парсит тело формы из строки запроса (application/x-www-form-urlencoded).
   * Возвращает Map параметров и их значений.
   */
  def parseForm(body: String): Map[String, String] =
    body.split("&").toList.collect {
      case pair if pair.contains("=") =>
        val Array(k, v) = pair.split("=", 2)
        URLDecoder.decode(k, "UTF-8") -> URLDecoder.decode(v, "UTF-8")
      case key =>
        URLDecoder.decode(key, "UTF-8") -> ""
    }.toMap

  /**
   * Парсит query-параметры из URL-запроса.
   * Возвращает Map параметров и их значений.
   */
  def parseQuery(query: String): Map[String, String] =
    if (query == null || query.trim.isEmpty) Map.empty
    else query.split("&").toList.collect {
      case pair if pair.contains("=") =>
        val Array(k, v) = pair.split("=", 2)
        URLDecoder.decode(k, "UTF-8") -> URLDecoder.decode(v, "UTF-8")
      case key =>
        URLDecoder.decode(key, "UTF-8") -> ""
    }.toMap

  // Функция для экранирования HTML-символов, чтобы предотвратить сломанный вывод
  def escapeHtml(text: String): String =
    text.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;")
}
