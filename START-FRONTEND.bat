@echo off
echo Installing frontend dependencies...
cd /d "%~dp0frontend"
call npm install
echo Starting NTG Frontend (React)...
call npm start
pause
