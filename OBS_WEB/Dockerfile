FROM openjdk:21-jdk
ADD target/OBS_WEB.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]