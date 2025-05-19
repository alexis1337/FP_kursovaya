error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/ApplicantFunctions.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/ApplicantFunctions.scala
empty definition using pc, found symbol in pc: `<none>`.
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 370
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/ApplicantFunctions.scala
text:
```scala
package functions

import models._
import data._
import database._
import scala.util.{Try, Success, Failure}

object ApplicantFunctions {
  
  def showApplicantMenu(): Unit = {
    val score = readInt("Введите ваши баллы ЕГЭ: ", 0, 300)
    val available = directions.filter(d => score >= d.passScore)

    if (available.isEmpty) println("К сожалению, с такими баллами н@@ет доступных направлений.")
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