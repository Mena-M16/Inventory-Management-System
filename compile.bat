@echo off
echo ============================================
echo  Compiling Inventory Management System
echo ============================================

set SRC=src\main\java
set OUT=target\classes
set RES=src\main\resources
set LIB=lib

REM Create output directory
if not exist %OUT% mkdir %OUT%

REM Copy resources
xcopy /Y /Q %RES%\* %OUT%\

REM Build classpath from all JARs in lib folder
set CP=%OUT%
for %%f in (%LIB%\*.jar) do set CP=!CP!;%%f

REM Enable delayed expansion for the CP variable
setlocal enabledelayedexpansion
set CP=%OUT%
for %%f in (%LIB%\*.jar) do set CP=!CP!;%%f

REM Collect all .java files
dir /s /b %SRC%\*.java > sources.txt

echo Compiling...
javac -source 11 -target 11 -cp "!CP!" -d %OUT% @sources.txt

if %errorlevel% neq 0 (
    echo.
    echo COMPILATION FAILED!
    del sources.txt
    pause
    exit /b 1
)

del sources.txt
echo.
echo Compilation successful!
echo Run:  run_manual.bat
pause
