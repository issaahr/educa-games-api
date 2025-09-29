FROM gradle:8.10-jdk17-alpine

WORKDIR /app

COPY . .

RUN chmod +x gradlew

EXPOSE 8080

ENTRYPOINT ["gradle", "bootRun", "--no-daemon"]
