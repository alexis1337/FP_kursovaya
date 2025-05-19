error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Main.scala:
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Main.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 199
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Main.scala
text:
```scala
@main def run(): Unit = {
  println("=== Справочная система ВУЗа ===")
  println("1. Найти студента")
  println("2. Найти преподавателя")
  println("3. Посмотреть расписание")
  println("4. Выхо@@д")

  scala.io.StdIn.readLine("Выберите пункт: ") match {
    case "1" => findStudent()
    case "2" => findProfessor()
    case "3" => showTimetable()
    case "4" => println("До свидания!")
    case _   => println("Неверный ввод")
  }
}


```


#### Short summary: 

empty definition using pc, found symbol in pc: 