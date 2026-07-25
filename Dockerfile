FROM maven:3.9.9-eclipse-temurin-23-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the project files to the container
COPY . /app/

# Use Maven to build the project
RUN mvn clean package -Pproduction

# Runtime stage to run the application
FROM eclipse-temurin:23-jdk-alpine AS runtime

WORKDIR /app

# Copy only the necessary JAR and files for runtime
COPY --from=build /app/target/*.jar /app/app.jar

# Expose the port
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]