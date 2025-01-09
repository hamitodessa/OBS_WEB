#FROM maven:3.8-eclipse-temurin-21 AS build
#WORKDIR /app
#COPY /OBS_WEB/pom.xml ./
#COPY /OBS_WEB/src ./src
#RUN mvn clean package -DskipTests
#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#COPY --from=build /app/target/OBS_WEB.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Build aşaması
# Build aşaması
FROM maven:3.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY /OBS_WEB/pom.xml ./
COPY /OBS_WEB/src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jdk
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends \
    ttf-mscorefonts-installer \
    && rm -rf /var/lib/apt/lists/*

COPY /OBS_WEB/src/static/fonts /usr/share/fonts/truetype/custom_fonts
RUN fc-cache -fv
COPY --from=build /app/target/OBS_WEB.jar app.jar

# Port ve giriş noktası
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]




#FROM openjdk:21-jdk
#WORKDIR /app
#COPY target/OBS_WEB.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]
