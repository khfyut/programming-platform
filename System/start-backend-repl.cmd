@echo off
setlocal

set "ROOT=%~dp0"
set "DB_HOST=192.168.59.133"
set "DB_PORT=3306"
set "DB_NAME=programming_system"
set "DB_USER=root"
set "DB_PASSWORD=123"
set "PROGRAMMING_JWT_SECRET=programming_system_2026_secret_key"
set "PROGRAMMING_AI_API_KEY=sk-d4dad64bf6d74d8389936ad37f1b2713"
set "SERVER_PORT=8080"

cd /d "%ROOT%"
"C:\Program Files\Java\jdk-17\bin\java.exe" -jar "%ROOT%target\programming-system-1.0.0.jar"
