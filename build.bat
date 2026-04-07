@echo off
setlocal

echo ============================================
echo  Personal Finance Tracker - Build Script
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

REM Prefer Maven Wrapper (no Maven installation required), fall back to system mvn
set MVN_CMD=
if exist "%~dp0mvnw.cmd" (
    set MVN_CMD=%~dp0mvnw.cmd
    echo [INFO] Using Maven Wrapper ^(mvnw.cmd^)
) else (
    where mvn >nul 2>&1
    if %errorlevel% equ 0 (
        set MVN_CMD=mvn
        echo [INFO] Using system Maven
    ) else (
        echo [ERROR] Maven not found. The Maven Wrapper should have been bundled.
        echo         If it is missing, please re-clone the repository or install Maven
        echo         from https://maven.apache.org/download.cgi and add it to PATH.
        pause
        exit /b 1
    )
)

echo [INFO] Cleaning previous build...
call %MVN_CMD% clean -q
if %errorlevel% neq 0 (
    echo [ERROR] Clean failed.
    pause
    exit /b 1
)

echo [INFO] Running tests...
call %MVN_CMD% test -q
if %errorlevel% neq 0 (
    echo [WARN] Some tests failed. Check target\surefire-reports for details.
)

echo [INFO] Building fat JAR (skip tests for speed)...
call %MVN_CMD% package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Build failed. Run without -q to see full output.
    pause
    exit /b 1
)

echo.
echo ============================================
echo  Build SUCCESSFUL!
echo  JAR  : target\personal-finance-tracker-1.0.0.jar
echo  Run  : run.bat
echo ============================================
echo.

pause
