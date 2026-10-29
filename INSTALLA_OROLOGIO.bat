@echo off
setlocal
cd /d "%~dp0zeppos"
call npm install --no-audit --no-fund
if errorlevel 1 goto errore
call npx --yes @zeppos/zeus-cli@1.9.3 preview
if errorlevel 1 goto errore
pause
exit /b 0
:errore
echo Installazione non completata. Conserva il messaggio precedente.
pause
exit /b 1
