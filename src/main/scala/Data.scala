import Models._

// Первоначальные тестовые данные (до БД)

object Data { 
  val students: List[Student] = List(
    Student("Иванов И.И.", "22-ИВТ-1", 2022, "Информатика", "ИРИТ"),
    Student("Петров П.П.", "24-М", 2024, "Экономика", "ИНЭУ"),
    Student("Сидорова А.А.", "22-ЭН-1", 2022, "Физика", "ИФХТиМ"),
    Student("Кузнецов Д.Д.", "23-ЭУТД-2", 2023, "Механика", "ИТС")
  )

  val faculties: List[String] = List("ИРИТ", "ИНЭЛ", "ИТС", "ИПТМ", "ИЯЭиТФ", "ИФХТиМ", "ИНЭУ")

  val professors: List[Professor] = List(
    Professor("Смирнов А.В.", "доцент", "smirnov@uni.ru", List("Математика"), "ИРИТ")
  )

  val timetables: List[GroupTimetable] = List(
    GroupTimetable("22-ИВТ-1", List(
      TimetableEntry("Понедельник", "08:00-09:35", "Математика", "Ауд. 6101"),
      TimetableEntry("Понедельник", "9:45-11:20", "Информатика", "Ауд. 6402"),
      TimetableEntry("Вторник", "07:30-09:05", "Физика", "Ауд. 4301")
    ))
  )

  val directions: List[Direction] = List(
    Direction("Функциональное программирование", "ИРИТ", 240),
    Direction("Экономика", "ИНЭУ", 210),
    Direction("Биотехнологии", "ИЯЭиТФ", 230),
    Direction("Сопромат", "ИТС", 200),
    Direction("Теплоэнергетика", "ИФХТиМ", 190)
  )
}