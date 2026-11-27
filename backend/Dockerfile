FROM maven:3.9.13-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY .config/ .config/
COPY pom.xml ./
COPY src/ src/

# Integration tests run separately against PostgreSQL; image builds need no database.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/target/footballAI-backend-1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=6 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
