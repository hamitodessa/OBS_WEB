# =========================
# OBS_WEB - Dockerfile (FINAL)
# - Maven dependency cache
# - Daha küçük runtime (JRE)
# - Fonts cache
# =========================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY OBS_WEB/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY OBS_WEB/src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY OBS_WEB/src/main/resources/static/fonts /usr/share/fonts/truetype/custom_fonts
RUN fc-cache -fv || true

COPY --from=build /app/target/OBS_WEB.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
