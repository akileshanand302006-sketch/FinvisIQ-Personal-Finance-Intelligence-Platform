@echo off
REM SmartFinance - Build and Run Script
REM =====================================

set JAVA_HOME=C:\Program Files\Java\jdk-21
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-26
)
set MVN="%~dp0tools\apache-maven-3.9.14\bin\mvn.cmd"

REM Configure FinvisIQ Cloud Database Environment Variables if not present
if "%DB_URL%"=="" set DB_URL=jdbc:mysql://mysql-18243dae-akileshanand302006-3318.a.aivencloud.com:20218/smart_finance_db?sslMode=REQUIRED
if "%DB_USERNAME%"=="" set DB_USERNAME=avnadmin
if "%DB_PASSWORD%"=="" (
    echo [FINVISIQ] DB_PASSWORD is not set in environment.
)

echo.
echo  ========================================
echo   FinvisIQ — Personal Finance Intelligence
echo  ========================================
echo.

REM Terminate any stale locked Java process to release file handles
taskkill /F /IM java.exe /T 2>nul

if "%1"=="build" (
    echo [BUILD] Compiling project...
    call %MVN% compile -q
    if %ERRORLEVEL% EQU 0 (
        echo [BUILD] Build successful!
    ) else (
        echo [BUILD] Build failed!
    )
    goto :end
)

if "%1"=="run" (
    echo [RUN] Starting SmartFinance...
    call %MVN% compile javafx:run
    goto :end
)

REM Default: compile and run
echo [BUILD] Compiling project...
call %MVN% compile javafx:run

:end
