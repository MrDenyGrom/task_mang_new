FROM openjdk:17-jdk-alpine

WORKDIR /app

# Шаг 1: Кэшируем зависимости
COPY ./mvnw /app/mvnw
COPY .mvn/ /app/.mvn/
COPY pom.xml /app/
RUN ./mvnw dependency:go-offline

# Шаг 2: Копируем исходный код
COPY src /app/src

# Шаг 3: Собираем приложение
RUN ./mvnw package -DskipTests

# Шаг 4: Запускаем
CMD ["java", "-jar", "target/task_management_system-0.0.1-SNAPSHOT.jar"]