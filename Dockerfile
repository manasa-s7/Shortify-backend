# Use Java 17 JDK as base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy all project files into container
COPY . .

# Build the Spring Boot project using Maven Wrapper
RUN ./mvnw clean package

# Expose port 8080 (the port your Spring Boot app runs on)
EXPOSE 8080

# Command to run the Spring Boot jar
CMD ["java", "-jar", "target/shortify-0.0.1-SNAPSHOT.jar"]
