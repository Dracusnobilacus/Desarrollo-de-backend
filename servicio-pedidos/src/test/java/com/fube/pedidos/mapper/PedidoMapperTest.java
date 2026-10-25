package com.fube.pedidos.mapper;

import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoMapperTest {

    @Test
    void testToDTO() {
        LocalDateTime now = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .id(1L)
                .clienteId(10L)
                .fechaCreacion(now)
                .estado(EstadoPedido.PENDIENTE)
                .total(new BigDecimal("1500.50"))
                .descripcion("Pedido de prueba")
                .build();

        PedidoDTO dto = PedidoMapper.toDTO(pedido);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getClienteId());
        assertEquals(now, dto.getFechaCreacion());
        assertEquals(EstadoPedido.PENDIENTE, dto.getEstado());
        assertEquals(new BigDecimal("1500.50"), dto.getTotal());
        assertEquals("Pedido de prueba", dto.getDescripcion());
    }

    @Test
    void testToEntity() {
        LocalDateTime now = LocalDateTime.now();
        PedidoDTO dto = PedidoDTO.builder()
                .id(2L)
                .clienteId(20L)
                .fechaCreacion(now)
                .estado(EstadoPedido.CONFIRMADO)
                .total(new BigDecimal("3200.00"))
                .descripcion("Pedido confirmado")
                .build();

        Pedido entity = PedidoMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(2L, entity.getId());
        assertEquals(20L, entity.getClienteId());
        assertEquals(now, entity.getFechaCreacion());
        assertEquals(EstadoPedido.CONFIRMADO, entity.getEstado());
        assertEquals(new BigDecimal("3200.00"), entity.getTotal());
        assertEquals("Pedido confirmado", entity.getDescripcion());
    }
}
