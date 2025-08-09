# OrgNotes (Android, Kotlin + Compose)

Приложение для заметок с двумя вариантами хранения: **SQLite (Room)** и **файлы (JSON)**.  
Подходит для фиксирования заметок на совещаниях, встречах и конференциях.

## Возможности
- Создание, редактирование, удаление и поиск заметок  
- Переключатель хранилища: SQLite ↔︎ файлы  
- Фильтр по дате/времени: пресеты (Сегодня/Неделя/Месяц) и ручной диапазон  
- Интуитивный интерфейс на Material 3 (Jetpack Compose)

## Технологии
Kotlin 2.0, Jetpack Compose (Material3, Navigation), Room, kotlinx-serialization, JDK 17, AGP 8.5+.

## Сборка и запуск
```bash
./gradlew assembleDebug
# или через Android Studio ▶
