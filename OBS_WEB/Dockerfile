FROM maven:3.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY /OBS_WEB/pom.xml ./
COPY /OBS_WEB/src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/OBS_WEB-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
