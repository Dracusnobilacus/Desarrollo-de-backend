-- =============================================================================
-- Script de inicialización de bases de datos para los tres microservicios
-- Fundamentos de Desarrollo Backend - UTN FR Santa Fe
-- =============================================================================
-- Instrucciones:
-- 1. Abrir pgAdmin o psql conectado al servidor PostgreSQL (usuario: postgres).
-- 2. Ejecutar este script sobre la base de datos por defecto 'postgres'.
-- 3. Una vez creadas las bases, cada microservicio ejecutará sus migraciones
--    Flyway automáticamente al iniciar.
-- =============================================================================

CREATE DATABASE clientesdb;
CREATE DATABASE inventariodb;
CREATE DATABASE pedidosdb;
