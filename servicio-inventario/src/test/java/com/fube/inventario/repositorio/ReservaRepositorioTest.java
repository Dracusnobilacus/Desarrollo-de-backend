package com.fube.inventario.repositorio;

import com.fube.inventario.modelos.EstadoReserva;
import com.fube.inventario.modelos.Producto;
import com.fube.inventario.modelos.Reserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ReservaRepositorioTest {

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    @Test
    @DisplayName("findByPedidoIdAndEstado devuelve solo reservas del pedido en el estado indicado")
    void findByPedidoIdAndEstado_filtraCorrectamente() {
        // GIVEN
        Producto producto = productoRepositorio.save(new Producto("Teclado", 20));
        reservaRepositorio.save(new Reserva(producto, 100L, 2, EstadoReserva.ACTIVA));
        reservaRepositorio.save(new Reserva(producto, 100L, 1, EstadoReserva.LIBERADA));
        reservaRepositorio.save(new Reserva(producto, 200L, 3, EstadoReserva.ACTIVA));

        // WHEN
        List<Reserva> activasPedido100 = reservaRepositorio.findByPedidoIdAndEstado(100L, EstadoReserva.ACTIVA);
        List<Reserva> todasPedido100 = reservaRepositorio.findByPedidoId(100L);
        List<Reserva> activasProducto = reservaRepositorio.findByProductoIdAndEstado(producto.getId(), EstadoReserva.ACTIVA);

        // THEN
        assertEquals(1, activasPedido100.size());
        assertEquals(2, todasPedido100.size());
        assertEquals(2, activasProducto.size());
    }
}
