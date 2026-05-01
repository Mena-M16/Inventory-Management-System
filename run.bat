@echo off
echo ============================================
echo  Inventory Management System - Windows
echo ============================================

REM Check if Maven is available
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven not found. Please install Maven and add it to PATH.
    pause
    exit /b 1
)

echo Building project...
mvn clean package -q

if %errorlevel% neq 0 (
    echo ERROR: Build failed. Check the output above.
    pause
    exit /b 1
)

echo Starting application...
java -jar target\inventory-management-system.jar

pause
