@echo off
echo ============================================
echo  Running Inventory Management System
echo ============================================

set OUT=target\classes
set LIB=lib

setlocal enabledelayedexpansion
set CP=%OUT%
for %%f in (%LIB%\*.jar) do set CP=!CP!;%%f

java -cp "!CP!" com.inventory.Main

pause
