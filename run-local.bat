@echo off
REM =======================================================================
REM FinvisIQ — Local Development Desktop Client Launcher
REM Connects to local Spring Boot backend on http://localhost:8085
REM =======================================================================

set API_BASE_URL=http://localhost:8085

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
echo   FinvisIQ — Desktop Client (Local Dev Gateway Mode)
echo   Target Backend: %API_BASE_URL%
echo  =======================================================
echo.

call %MVN% compile javafx:run

:end
