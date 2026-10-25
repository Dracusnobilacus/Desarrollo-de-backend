CREATE TABLE pedidos (
    id             BIGSERIAL PRIMARY KEY,
    cliente_id     BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado         VARCHAR(50) NOT NULL,
    total          NUMERIC(12, 2) NOT NULL,
    descripcion    VARCHAR(500)
);
