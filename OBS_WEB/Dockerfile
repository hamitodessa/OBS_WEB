FROM maven:3.8-eclipse-temurin-21 AS build
ENV MYSQL_DATABASE=obs_user
ENV MYSQL_USER=hamit
ENV MYSQL_PASSWORD=oOk271972
ENV MYSQL_ROOT_PASSWORD=rootpassword
WORKDIR /app
COPY /OBS_WEB/pom.xml ./
COPY /OBS_WEB/src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/OBS_WEB.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


#FROM openjdk:21-jdk
#WORKDIR /app
#COPY target/OBS_WEB.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]
