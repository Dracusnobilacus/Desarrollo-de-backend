
-- Tabla: pedidos
CREATE TABLE pedidos (
    id                  BIGSERIAL PRIMARY KEY,
    cliente_id          BIGINT NOT NULL,
    estado              VARCHAR(50) NOT NULL,
    monto_total         NUMERIC(12, 2) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para la tabla pedidos
CREATE INDEX idx_pedidos_cliente_id ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_fecha_creacion ON pedidos(fecha_creacion);

-- Tabla: item_pedidos
CREATE TABLE item_pedidos (
    id              BIGSERIAL PRIMARY KEY,
    pedido_id       BIGINT NOT NULL,
    producto_id     BIGINT NOT NULL,
    cantidad        INT NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_item_pedidos_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE
);

-- Índices para la tabla item_pedidos
CREATE INDEX idx_item_pedidos_pedido_id ON item_pedidos(pedido_id);
CREATE INDEX idx_item_pedidos_producto_id ON item_pedidos(producto_id);
