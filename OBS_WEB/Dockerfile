# =========================
# OBS_WEB - Dockerfile (FINAL)
# - Maven dependency cache
# - Daha küçük runtime (JRE)
# - Fonts cache
# =========================

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) dependency cache
COPY OBS_WEB/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# 2) source + build
COPY OBS_WEB/src ./src
RUN mvn -q -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Fonts
COPY OBS_WEB/src/main/resources/static/fonts /usr/share/fonts/truetype/custom_fonts
RUN fc-cache -fv || true

# App
COPY --from=build /app/target/OBS_WEB.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
