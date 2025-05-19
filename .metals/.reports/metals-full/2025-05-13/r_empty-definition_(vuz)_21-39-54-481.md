error id: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala:
file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -students.
	 -scala/Predef.students.
offset: 104
uri: file:///C:/Users/Alexis/AppData/Local/Temp/vuz/src/main/scala/Functions.scala
text:
```scala
object Functions {
def findStudentByFacultyAndYear(faculty: String, year: Int): List[Student] = {
  stud@@ents.filter(s => s.faculty == faculty && s.year == year)
}

def showStudentSearch(): Unit = {
  val faculty = scala.io.StdIn.readLine("Факультет: ")
  val year = scala.io.StdIn.readLine("Год поступления: ").toInt
  val found = findStudentByFacultyAndYear(faculty, year)
  found.foreach(s => println(s"${s.name} — ${s.group}"))
}
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 