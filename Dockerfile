FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY *.jar /app/app.jar

EXPOSE 8090

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]