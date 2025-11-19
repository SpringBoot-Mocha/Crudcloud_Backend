# Stage 1: Build stage
FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .

# Build the application
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/CrudCloud-0.0.1-SNAPSHOT.jar app.jar

# Expose port (will be overridden by docker-compose)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/plans || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
