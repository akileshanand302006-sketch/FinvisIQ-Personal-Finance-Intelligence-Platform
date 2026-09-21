@echo off
REM =======================================================================
REM FinvisIQ — Personal Finance Intelligence Platform
REM Downloadable / Local Desktop Client Launcher
REM Secure Cloud API Mode — No DB credentials required
REM =======================================================================

if not "%JAVA_HOME%"=="" goto java_ok
if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    goto java_ok
)
if exist "C:\Program Files\Java\jdk-26\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-26"
    goto java_ok
)
if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    goto java_ok
)
:java_ok

set MVN="%~dp0tools\apache-maven-3.9.14\bin\mvn.cmd"

echo.
echo  =======================================================
echo   FinvisIQ — Personal Finance Intelligence Platform
echo   Desktop Application (Cloud REST Gateway Mode)
echo  =======================================================
echo.

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
    echo [RUN] Starting FinvisIQ Desktop...
    call %MVN% javafx:run
    goto :end
)

REM Default: compile and run
echo [BUILD] Starting FinvisIQ Desktop...
call %MVN% compile javafx:run

:end
