@echo off
setlocal
cd /d "%~dp0zeppos"
call npm install --no-audit --no-fund
if errorlevel 1 goto errore
node ..\scripts\setup-zeus.cjs
if errorlevel 1 goto errore
node ..\scripts\make_icon.cjs
if errorlevel 1 goto errore
call npx --no-install zeus preview
if errorlevel 1 goto errore
pause
exit /b 0
:errore
echo Installazione non completata. Conserva il messaggio precedente.
pause
exit /b 1
