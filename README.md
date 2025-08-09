Приложение для заметок с двумя хранилищами: SQLite (Room) и файлы (JSON).

## Возможности
- Создание, редактирование, удаление, поиск заметок  
- Переключение хранилища: SQLite ↔️ файлы  
- Фильтр по дате/времени (пресеты + ручной диапазон)  
- Интуитивный интерфейс на Material 3

## Технологии
Kotlin 2.0, Jetpack Compose, Material 3, Navigation, Room, kotlinx-serialization, JDK 17, AGP 8.5+.

## Сборка/запуск
```bash
./gradlew assembleDebug
# или через Android Studio ▶


mkdir -p .github/workflows

cat > .github/workflows/android-ci.yml << 'EOF'
name: Android CI

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Validate Gradle Wrapper
        uses: gradle/wrapper-validation-action@v2

      - name: Setup JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build & Lint
        run: ./gradlew clean lint assembleDebug --stacktrace --no-daemon
