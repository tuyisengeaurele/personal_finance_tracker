@echo off
setlocal

echo ============================================
echo  Personal Finance Tracker
echo ============================================
echo.

REM Check Java 17+
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install Java 17 or later from https://adoptium.net
    pause
    exit /b 1
)

REM Resolve JAR path
set JAR=target\personal-finance-tracker-1.0.0.jar

if not exist "%JAR%" (
    echo [INFO] JAR not found. Building first...
    call build.bat
    if %errorlevel% neq 0 exit /b 1
)

echo [INFO] Starting Personal Finance Tracker...
java -jar "%JAR%"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application exited with error code %errorlevel%.
    echo        Check %USERPROFILE%\.financetracker\finance-tracker.log for details.
    pause
)
