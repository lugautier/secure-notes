# Stage 1: Build (compile)
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy pre-built JAR (built locally with mvn clean package)
# In a true multi-stage, we'd compile here, but for local dev we use pre-built JAR
COPY target/*.jar app.jar

# Stage 2: Runtime (minimal image)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# Copy pre-built JAR from builder stage
COPY --from=builder /app/app.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher &>/dev/null || exit 1

# Expose port (default Spring Boot port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
