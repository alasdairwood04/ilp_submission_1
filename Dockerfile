# ------------ Stage 1: Build ------------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy only pom.xml first (for dependency caching)
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Build the application (generates target/demo-api-0.0.1-SNAPSHOT.jar)
RUN mvn clean package -DskipTests

# ------------ Stage 2: Run ------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the built JAR from builder stage
COPY --from=builder /app/target/demo-api-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
