# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .

# Download dependencies with retry logic and verbose output
RUN mvn dependency:resolve dependency:resolve-plugins -B -e || \
    (echo "First attempt failed, retrying..." && sleep 5 && mvn dependency:resolve dependency:resolve-plugins -B -e)

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -e

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install Docker CLI for Docker-in-Docker capability
RUN apk add --no-cache docker-cli

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set the Spring profile to docker
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
