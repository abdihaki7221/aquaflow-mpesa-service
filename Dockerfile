# ═══════════════════════════════════════════════════════════════
# Dockerfile — AquaFlow M-Pesa Service (Cloud Run ready)
# Replace your existing Dockerfile with this one.
# ═══════════════════════════════════════════════════════════════

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Non-root user (good practice)
RUN useradd -ms /bin/bash appuser
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser

# Default profile is cloudrun (Cloud Run also sets this via env var).
ENV SPRING_PROFILES_ACTIVE=cloudrun
# Let the JVM see the container's memory limits.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# NOTE: no -Dserver.port here. The app reads PORT from the environment
# via server.port=${PORT:8080}, which Cloud Run always provides.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
