FROM openjdk:21-jdk
WORKDIR /app
COPY target/OBS_WEB.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]