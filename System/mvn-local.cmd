@echo off
setlocal

set "MVN_CMD="

if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
if not defined MVN_CMD if defined M2_HOME if exist "%M2_HOME%\bin\mvn.cmd" set "MVN_CMD=%M2_HOME%\bin\mvn.cmd"
if not defined MVN_CMD for %%I in (mvn.cmd) do set "MVN_CMD=%%~$PATH:I"

if not defined MVN_CMD if exist "D:\IntelliJ IDEA 2024.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" set "MVN_CMD=D:\IntelliJ IDEA 2024.2.1\plugins\maven\lib\maven3\bin\mvn.cmd"
if not defined MVN_CMD if exist "D:\IntelliJ IDEA Community Edition 2024.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" set "MVN_CMD=D:\IntelliJ IDEA Community Edition 2024.1.4\plugins\maven\lib\maven3\bin\mvn.cmd"
if not defined MVN_CMD if exist "%ProgramFiles%\JetBrains\IntelliJ IDEA\plugins\maven\lib\maven3\bin\mvn.cmd" set "MVN_CMD=%ProgramFiles%\JetBrains\IntelliJ IDEA\plugins\maven\lib\maven3\bin\mvn.cmd"

if not defined MVN_CMD (
  echo Maven not found. Please configure PATH, MAVEN_HOME, or M2_HOME.
  exit /b 1
)

set "MAVEN_REPO=%USERPROFILE%\.m2\repository"

if exist "%MAVEN_REPO%" (
  call "%MVN_CMD%" -Dmaven.repo.local="%MAVEN_REPO%" %*
) else (
  call "%MVN_CMD%" %*
)
