#!/bin/bash

# Check if the application is running
if pgrep -f "pnpm" > /dev/null; then
    echo "Stopping existing process..."
    # Get the process ID (PID) of the running application
    PID=$(pgrep -f "pnpm")
    # Kill the existing process
    kill -9 $PID
    echo "Stopped process with PID: $PID"
else
    echo "No existing process found."
fi

# Run the new JAR file in the background
echo "Starting the new application..."
nohup pnpm dev > /dev/null 2>&1 &
# running pnpm dev because failing in build due to some issues `will fix later`

echo "Application started in the background."
