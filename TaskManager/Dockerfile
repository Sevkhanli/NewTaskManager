# 1. Build mərhələsi: Maven və Java 21 ilə layihəni yığırıq
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Run mərhələsi: Yaranan jar faylını Java 21 JRE ilə işə salırıq
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]