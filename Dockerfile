# ------------ Stage 1: Build ------------

# Use a Maven image that includes Java 21
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only pom.xml first (for dependency caching)
COPY pom.xml .
# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ------------ Stage 2: Run ------------

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy only the built JAR from builder stage
COPY --from=builder /app/target/ilp_submission_image-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]