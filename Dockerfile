FROM maven:4.0.0-rc-5-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw

RUN mvn -q -DskipTests dependency:go-offline

COPY src src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar
COPY data data

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
