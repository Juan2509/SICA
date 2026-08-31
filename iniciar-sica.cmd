@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0iniciar-sica.ps1"
if errorlevel 1 (
    echo.
    echo No se pudo preparar SICA. Revisa el mensaje anterior.
    pause
)
