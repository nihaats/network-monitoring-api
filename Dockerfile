# 1️⃣ Build aşaması
FROM maven:3.9.10-eclipse-temurin-21 AS build
WORKDIR /app

# Maven cache için önce pom dosyalarını kopyala
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sonra proje kaynaklarını kopyala
COPY src ./src

# Maven ile package (jar) oluştur
RUN mvn clean package -DskipTests

# 2️⃣ Run aşaması (minimal JRE ile)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Build aşamasından jar dosyasını al
COPY --from=build /app/target/*.jar app.jar

# Portu aç
EXPOSE 8090

# JVM optimizasyonları ile çalıştır
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]