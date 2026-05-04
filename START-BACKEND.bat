@echo off
echo Starting NTG Backend (Spring Boot)...
cd /d "%~dp0backend"
mvn spring-boot:run
pause
