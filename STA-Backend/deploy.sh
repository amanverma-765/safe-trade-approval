#!/bin/bash

# Define the name of your JAR file (replace with your actual JAR name)
JAR_NAME="STA-Backend-0.0.1-SNAPSHOT.jar"
echo "Deleting old jar file"
rm -rf build/libs/$JAR_NAME

# Run the build command
./gradlew build

# Check if the application is running
if pgrep -f "$JAR_NAME" > /dev/null; then
    echo "Stopping existing process..."
    # Get the process ID (PID) of the running application
    PID=$(pgrep -f "$JAR_NAME")
    # Kill the existing process
    kill -9 $PID
    echo "Stopped process with PID: $PID"
else
    echo "No existing process found."
fi

# Run the new JAR file in the background
echo "Starting the new application..."
nohup java -jar build/libs/$JAR_NAME > /dev/null 2>&1 &

echo "Application started in the background."
