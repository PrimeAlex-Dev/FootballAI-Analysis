FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY .config/ .config/
COPY mvnw pom.xml ./
COPY src/ src/
# Retry only Wrapper initialization for transient download failures (for example HTTP 429).
RUN --mount=type=cache,target=/root/.m2 \
    for attempt in 1 2 3; do \
        if sh ./mvnw --version; then \
            exit 0; \
        fi; \
        echo "Maven Wrapper initialization failed (attempt ${attempt}/3)."; \
        if [ "$attempt" -lt 3 ]; then sleep "$((attempt * 10))"; fi; \
    done; \
    exit 1

# Integration tests run separately against PostgreSQL; image builds need no database.
RUN --mount=type=cache,target=/root/.m2 \
    sh ./mvnw --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/target/footballAI-backend-1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=6 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
