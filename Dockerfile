# Build the application
FROM gradle:8.0-jdk17 AS build
# Copy Gradle wrapper and project files
COPY . .
# Build the application without running tests
RUN gradle clean build -x test --no-daemon

# Create a minimal runtime image
FROM eclipse-temurin:17-jdk-jammy
# Copy the built JAR file from the builder stage
COPY --from=build /home/gradle/build/libs/PlacementManagementSystem-0.0.1-SNAPSHOT.jar PlacementManagementSystem.jar
# Expose the application's port
EXPOSE 8080
# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "PlacementManagementSystem.jar"]