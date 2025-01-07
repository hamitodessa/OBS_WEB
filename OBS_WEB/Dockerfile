FROM mysql:8.0.33 AS mysqldb
ENV MYSQL_DATABASE=obs_user
ENV MYSQL_USER=hamit
ENV MYSQL_PASSWORD=oOk271972
ENV MYSQL_ROOT_PASSWORD=rootpassword
FROM eclipse-temurin:21-jdk AS app
WORKDIR /app
COPY target/obs_web-0.0.1.jar app.jar
CMD ["sh", "-c", "/usr/bin/mysqld_safe & java -jar app.jar"]



#FROM openjdk:21-jdk
#WORKDIR /app
#COPY target/OBS_WEB.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]
