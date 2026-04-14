@echo off
setlocal

set "ROOT=%~dp0"
set "LOG=%ROOT%backend-8080-run.log"

set "DB_HOST=192.168.59.133"
set "DB_PORT=3306"
set "DB_NAME=programming_system"
set "DB_USER=root"
set "DB_PASSWORD=123"
set "PROGRAMMING_JWT_SECRET=programming_system_2026_secret_key"

pushd "%ROOT%"
start "backend8080" /min cmd /c ""%ROOT%mvn-local.cmd" -q spring-boot:run > "%LOG%" 2>&1"
popd

endlocal
