# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY build/libs/*.jar app.jar

# Create a non-root user
RUN groupadd -r codin && useradd -r -g codin codin
RUN chown -R codin:codin /app
USER codin

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/api/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
