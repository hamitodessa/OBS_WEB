FROM openjdk:21-jdk
WORKDIR /app
COPY OBS_WEB/target/OBS_WEB.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]