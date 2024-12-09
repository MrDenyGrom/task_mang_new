FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY ./mvnw /app/mvnw
COPY .mvn/ /app/.mvn/
COPY pom.xml /app/
RUN ./mvnw dependency:go-offline

COPY src /app/src
COPY application.yml /app/

RUN ./mvnw package -DskipTests

CMD ["java", "-jar", "target/task_management_system-0.0.1-SNAPSHOT.jar"]