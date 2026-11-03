@echo off
title Sistema de Microservicios TiendaOnline
echo ========================================================
echo  Iniciando los 3 microservicios en ventanas separadas...
echo ========================================================

start "1. Servicio Clientes (Puerto 8080)" cmd /k ".\gradlew bootRun"
timeout /t 3 /nobreak > nul

start "2. Servicio Inventario (Puerto 8081)" cmd /k "cd servicio-inventario && ..\gradlew bootRun"
timeout /t 3 /nobreak > nul

start "3. Servicio Pedidos (Puerto 8082)" cmd /k "cd servicio-pedidos && ..\gradlew bootRun"

echo.
echo ========================================================
echo  Servicios iniciados. Swagger UI disponible en:
echo   - Clientes:   http://localhost:8080/swagger-ui/index.html
echo   - Inventario: http://localhost:8081/swagger-ui/index.html
echo   - Pedidos:    http://localhost:8082/swagger-ui/index.html
echo ========================================================
echo.
pause

