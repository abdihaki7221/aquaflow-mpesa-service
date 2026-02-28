# Stage 1: Build (keep as-is)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run (switch to non-Alpine)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Add non-root user for security (Debian syntax)
RUN useradd -ms /bin/bash appuser

COPY --from=build /app/target/*.jar app.jar

RUN chown appuser:appuser app.jar
USER appuser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=railway
#
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -Dserver.port=${PORT} -Dserver.address=0.0.0.0 -jar app.jar"]