file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Console.scala
empty definition using pc, found symbol in pc: 
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 26
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Console.scala
text:
```scala
//> using dep "org.typelev@@el::cats-effect:3.5.3"


import cats.effect.IO

// Typeclass для абстрагирования ввода/вывода
trait Console[F[_]] {
  def println(msg: String): F[Unit]
  def readLine(prompt: String): F[String]
}

object Console {
  def apply[F[_]](implicit ev: Console[F]): Console[F] = ev

  // Реализация для IO
  implicit val ioConsole: Console[IO] = new Console[IO] {
    def println(msg: String): IO[Unit] = IO(Predef.println(msg))
    def readLine(prompt: String): IO[String] = IO {
      Predef.print(prompt)
      scala.io.StdIn.readLine()
    }
  }
}


```


#### Short summary: 

empty definition using pc, found symbol in pc: 