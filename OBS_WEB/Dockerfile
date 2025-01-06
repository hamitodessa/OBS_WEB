# Maven Build Aşaması
FROM maven:3.8-openjdk-17 AS build

# Çalışma dizinini ayarla
WORKDIR /app

# Sadece gerekli dosyaları kopyala
COPY pom.xml ./
COPY src ./src

# Maven ile projeyi build et
RUN mvn clean package -DskipTests

# Runtime Aşaması
FROM openjdk:17-jdk-slim
WORKDIR /app

# Maven build çıktısını kopyala
COPY --from=build /app/target/OBS_WEB-0.0.1-SNAPSHOT.jar app.jar

# Uygulamayı çalıştır
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


