-- Actualizar tabla pedidos para coincidir con la entidad Pedido
ALTER TABLE pedidos RENAME COLUMN total TO monto_total;
ALTER TABLE pedidos ADD COLUMN fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE pedidos DROP COLUMN IF EXISTS descripcion;

-- Crear tabla items_pedido para la entidad ItemPedido
CREATE TABLE items_pedido (
    id              BIGSERIAL PRIMARY KEY,
    pedido_id       BIGINT NOT NULL,
    producto_id     BIGINT NOT NULL,
    cantidad        INT NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_items_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE
);

CREATE INDEX idx_items_pedido_pedido_id ON items_pedido(pedido_id);
