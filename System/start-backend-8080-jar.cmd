@echo off
setlocal

set "ROOT=%~dp0"
set "LOG=%ROOT%backend-8080-run.log"
set "JAR=%ROOT%target\programming-system-1.0.0.jar"

set "DB_HOST=192.168.59.133"
set "DB_PORT=3306"
set "DB_NAME=programming_system"
set "DB_USER=root"
set "DB_PASSWORD=123"
set "PROGRAMMING_JWT_SECRET=programming_system_2026_secret_key"

if not exist "%JAR%" (
  echo Jar not found: %JAR%
  exit /b 1
)

pushd "%ROOT%"
start "backend8080jar" /min cmd /c ""C:\Program Files\Java\jdk-17\bin\java.exe" -jar "%JAR%" > "%LOG%" 2>&1"
popd

endlocal
