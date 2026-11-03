@echo off
title Detener Microservicios TiendaOnline
echo ========================================================
echo  Deteniendo procesos en puertos 8080, 8081 y 8082...
echo ========================================================

for %%p in (8080 8081 8082) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%p ^| findstr LISTENING') do (
        echo Deteniendo proceso %%a en puerto %%p...
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo.
echo ========================================================
echo  Todos los servicios han sido detenidos.
echo ========================================================
pause

