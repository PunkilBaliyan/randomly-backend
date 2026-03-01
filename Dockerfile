FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Set default port to 8080, but allow override via PORT environment variable
ENV PORT=8080

CMD ["sh", "-c", "java -jar app.jar"]
