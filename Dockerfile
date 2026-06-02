FROM maven:3.8.6-eclipse-temurin-11-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/target/sse-demo-1.0.0.jar app.jar
COPY data.json .
EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
