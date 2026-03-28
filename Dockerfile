# Multi-stage build for smaller image size

# Stage 1: Build
FROM gradle:9.4.1-jdk21 AS build

# Use the pre-installed Gradle distribution from the base image (avoids re-download)
ENV GRADLE_USER_HOME=/home/gradle/.gradle

WORKDIR /app

# Copy gradle files first for better layer caching
COPY gradlew ./
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

# Download dependencies (cached layer if build.gradle doesn't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application (skip tests in Docker build, run them separately)
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create uploads directory with proper permissions
RUN mkdir -p /app/uploads && chown -R spring:spring /app/uploads

USER spring:spring

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check (requires Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with optimized JVM settings
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
