error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Main.scala:scala/Predef.println(+1).
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Main.scala
empty definition using pc, found symbol in pc: 
found definition using semanticdb; symbol scala/Predef.println(+1).
empty definition using fallback
non-local guesses:

offset: 35
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Main.scala
text:
```scala
@main def run(): Unit = {
  printl@@n("=== Справочная система ВУЗа ===")
  println("1. Найти студента")
  println("2. Найти преподавателя")
  println("3. Посмотреть расписание")
  println("4. Выход")

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