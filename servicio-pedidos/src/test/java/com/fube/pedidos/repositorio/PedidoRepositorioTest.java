package com.fube.pedidos.repositorio;

import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class PedidoRepositorioTest {

    @Autowired
    private PedidoRepositorio repositorio;

    @Test
    @DisplayName("findByEstadoAndFechaCreacionBefore devuelve solo pedidos vencidos en el estado indicado")
    void findByEstadoAndFechaCreacionBefore_filtraCorrectamente() {
        // GIVEN
        LocalDateTime hace30Horas = LocalDateTime.now().minusHours(30);
        LocalDateTime hace2Horas = LocalDateTime.now().minusHours(2);

        repositorio.save(Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("100.00"))
                .fechaCreacion(hace30Horas)
                .fechaActualizacion(hace30Horas)
                .build());

        repositorio.save(Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("200.00"))
                .fechaCreacion(hace2Horas)
                .fechaActualizacion(hace2Horas)
                .build());

        repositorio.save(Pedido.builder()
                .clienteId(2L)
                .estado(EstadoPedido.CONFIRMADO)
                .montoTotal(new BigDecimal("300.00"))
                .fechaCreacion(hace30Horas)
                .fechaActualizacion(hace30Horas)
                .build());

        // WHEN
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        List<Pedido> vencidos = repositorio.findByEstadoAndFechaCreacionBefore(EstadoPedido.CREADO, limite);
        List<Pedido> delCliente1 = repositorio.findByClienteId(1L);
        List<Pedido> confirmados = repositorio.findByEstado(EstadoPedido.CONFIRMADO);

        // THEN
        assertEquals(1, vencidos.size());
        assertEquals(2, delCliente1.size());
        assertEquals(1, confirmados.size());
    }
}
