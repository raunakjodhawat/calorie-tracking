# Use OpenJDK 11 as base
FROM openjdk:11-jdk-slim

# Install sbt
RUN apt-get update && \
    apt-get install -y curl && \
    curl -L -o /usr/local/bin/sbt https://github.com/sbt/sbt/releases/download/v1.8.2/sbt-1.8.2 && \
    chmod +x /usr/local/bin/sbt

WORKDIR /app

# Copy the project
COPY . /app

# Download dependencies and compile
RUN sbt compile

# Default command: run the app
CMD ["sbt", "run"] 