CREATE TABLE productos (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    cantidad_disponible INT NOT NULL CHECK (cantidad_disponible >= 0)
);

CREATE TABLE reservas (
    id          BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    pedido_id   BIGINT NOT NULL,
    cantidad    INT NOT NULL CHECK (cantidad > 0),
    estado      VARCHAR(50) NOT NULL,
    CONSTRAINT fk_reservas_producto FOREIGN KEY (producto_id) REFERENCES productos (id)
);

CREATE INDEX idx_reservas_pedido ON reservas (pedido_id);
CREATE INDEX idx_reservas_producto ON reservas (producto_id);
