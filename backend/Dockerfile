FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn

RUN mvn -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
