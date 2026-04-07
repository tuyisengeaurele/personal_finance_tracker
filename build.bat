@echo off
setlocal

echo ============================================
echo  Personal Finance Tracker - Build Script
echo ============================================
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install Java 17 or later.
    pause
    exit /b 1
)

REM Check Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found. Please install Apache Maven 3.8+.
    pause
    exit /b 1
)

echo [INFO] Cleaning previous build...
call mvn clean -q
if %errorlevel% neq 0 (
    echo [ERROR] Clean failed.
    pause
    exit /b 1
)

echo [INFO] Running tests...
call mvn test -q
if %errorlevel% neq 0 (
    echo [WARN] Some tests failed. Check target/surefire-reports for details.
)

echo [INFO] Building fat JAR...
call mvn package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Build failed. Check the output above.
    pause
    exit /b 1
)

echo.
echo ============================================
echo  Build SUCCESSFUL!
echo  JAR: target\personal-finance-tracker-1.0.0.jar
echo  Run: run.bat
echo ============================================
echo.

pause
