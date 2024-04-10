# Use the official OpenJDK image as the base image
FROM openjdk:11

# Set the maintainer information
LABEL maintainer="felipe_caldas"

# Install necessary packages (if using Debian-based image)
# RUN apt-get update && apt-get install -y findutils && rm -rf /var/lib/apt/lists/*

# Set the working directory inside the container
WORKDIR /app

# Copy the contents of the current directory (including the JAR file) to the /app directory in the container
COPY . /app

# Run the Gradle build command to build the project
RUN ./gradlew build

# Expose port 8080 (assuming your application listens on this port)
EXPOSE 8080

# Specify the command to run your application when the container starts
ENTRYPOINT ["java", "-jar", "purchase-transaction-service.jar"]