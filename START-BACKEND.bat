@echo off
echo Starting Backend (Spring Boot)...
cd /d "%~dp0backend"
mvn spring-boot:run
pause
