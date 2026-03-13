# =============================================
# CrimeLink Analyzer - Spring Boot Backend
# Multi-stage build: Maven → JRE 21
# =============================================

# Stage 1: Build with Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and POM first (better caching)
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime with minimal JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Create directories for backups and uploads
RUN mkdir -p /app/backups && chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
