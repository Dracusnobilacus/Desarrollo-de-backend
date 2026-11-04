@echo off
title Ejecutar Batch Expiracion Pedidos
echo ========================================================
echo  Ejecutando Job de Spring Batch (expirarPedidosJob)...
echo ========================================================
cd servicio-pedidos && ..\gradlew bootRun --args="--spring.batch.job.enabled=true"
pause

