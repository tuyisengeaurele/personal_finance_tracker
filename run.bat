@echo off
setlocal

echo ============================================
echo  Personal Finance Tracker
echo ============================================
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install Java 17 or later from:
    echo         https://adoptium.net
    pause
    exit /b 1
)

REM Resolve JAR path
set JAR=%~dp0target\personal-finance-tracker-1.0.0.jar

if not exist "%JAR%" (
    echo [INFO] JAR not found. Building the project first...
    call "%~dp0build.bat"
    if %errorlevel% neq 0 (
        echo [ERROR] Build failed. Cannot launch application.
        pause
        exit /b 1
    )
)

echo [INFO] Starting Personal Finance Tracker...
java -jar "%JAR%"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application exited with code %errorlevel%.
    echo         Check log: %USERPROFILE%\.financetracker\finance-tracker.log
    pause
)
