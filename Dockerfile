FROM gradle:8.10-jdk17-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew \
    && ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE ${PORT:-8080}

HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -f http://localhost:${PORT:-8080}/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
