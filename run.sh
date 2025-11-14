#!/bin/bash

# Script to run the Multiple Choice Quiz application

echo "Starting Multiple Choice Quiz Application..."
echo "==========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null
then
    echo "Error: Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null
then
    echo "Error: Java is not installed. Please install Java 11 or higher."
    exit 1
fi

# Run the application
mvn clean compile exec:java -Dexec.mainClass="com.csc207.arcade.multiplechoice.app.Main"
