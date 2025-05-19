error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/UtilityFunctions.scala:`<none>`.
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/UtilityFunctions.scala
empty definition using pc, found symbol in pc: `<none>`.
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 735
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/functions/UtilityFunctions.scala
text:
```scala
package functions

import functions.StudentFunctions._

object UtilityFunctions {
def readInt(prompt: String, min: Int = Int.MinValue, max: Int = Int.MaxValue): Int = {
  Iterator.continually {
    println(prompt)
    val input = scala.io.StdIn.readLine()
    input.toIntOption match {
      case Some(num) if num >= min && num <= max => Some(num)
      case _ =>
        println(s"Ошибка: введите число от $min до $max.")
        None
    }
  }.collectFirst { case Some(value) => value }.get
}

  def chooseFaculty(faculties: List[String]): String = {
    Iterator.continually {
      faculties.zipWithIndex.foreach { case (facultyName, index) => println(s"${index + 1}. $facultyName") }
      val index = readInt("Введите номер факул@@ьтета: ", 1, faculties.length)
      faculties(index - 1)
    }.next()
  }

  def isConnected: Boolean = {
    Try(getAllStudents()).isSuccess
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.