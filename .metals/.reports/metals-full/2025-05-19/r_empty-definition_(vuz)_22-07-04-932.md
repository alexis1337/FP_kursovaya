error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/Functions.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/Functions.scala
empty definition using pc, found symbol in pc: `<none>`.
semanticdb not found
empty definition using fallback
non-local guesses:
	 -models/data.
	 -data/data.
	 -database/data.
	 -data.
	 -scala/Predef.data.
offset: 23
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/Functions.scala
text:
```scala
import models._
import @@data._
import database._
import scala.util.{Try, Success, Failure}

object Functions {
  
  def showApplicantMenu(): Unit = {
    val score = readInt("Введите ваши баллы ЕГЭ: ", 0, 300)
    val available = directions.filter(d => score >= d.passScore)

    if (available.isEmpty) println("К сожалению, с такими баллами нет доступных направлений.")
    else {
      println("Вы можете поступить на следующие направления:")
      available.foreach(d => println(s"- ${d.name} (${d.faculty}), проходной балл: ${d.passScore}"))
    }

    println("\nНажмите Enter, чтобы вернуться в меню...")
    scala.io.StdIn.readLine()
  }  

  
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.