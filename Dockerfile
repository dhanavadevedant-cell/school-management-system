FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# This safely checks the root target folder or a subfolder target folder
COPY target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]