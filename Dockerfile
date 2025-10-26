# Use Java 17 JDK as base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy all project files into the container
COPY . .

# Make mvnw executable
RUN chmod +x ./mvnw

# Build the Spring Boot project using Maven Wrapper
RUN ./mvnw clean package -DskipTests

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "target/shortify-0.0.1-SNAPSHOT.jar"]
