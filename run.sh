#!/bin/bash
echo "============================================"
echo " Inventory Management System - Linux/Mac"
echo "============================================"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found. Please install Maven."
    exit 1
fi

echo "Building project..."
mvn clean package -q

if [ $? -ne 0 ]; then
    echo "ERROR: Build failed."
    exit 1
fi

echo "Starting application..."
java -jar target/inventory-management-system.jar
