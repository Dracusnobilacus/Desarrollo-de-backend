package com.fube.pedidos.mapper;

import com.fube.pedidos.dto.*;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.ItemPedido;
import com.fube.pedidos.modelos.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoMapperTest {

    @Test
    void testCrearPedidoRequestDTOToEntity() {
        ItemPedidoRequestDTO itemReq = ItemPedidoRequestDTO.builder()
                .productoId(42L)
                .cantidad(3)
                .precioUnitario(new BigDecimal("150.00"))
                .build();

        CrearPedidoRequestDTO request = CrearPedidoRequestDTO.builder()
                .clienteId(5L)
                .items(List.of(itemReq))
                .build();

        Pedido entity = PedidoMapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(5L, entity.getClienteId());
        assertEquals(new BigDecimal("450.00"), entity.getMontoTotal());
        assertEquals(1, entity.getItems().size());
        assertEquals(42L, entity.getItems().get(0).getProductoId());
        assertEquals(3, entity.getItems().get(0).getCantidad());
        assertEquals(entity, entity.getItems().get(0).getPedido());
    }

    @Test
    void testEntityToPedidoResponseDTO() {
        LocalDateTime now = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .id(10L)
                .clienteId(7L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("500.00"))
                .fechaCreacion(now)
                .fechaActualizacion(now)
                .build();

        ItemPedido item = ItemPedido.builder()
                .id(1L)
                .productoId(100L)
                .cantidad(2)
                .precioUnitario(new BigDecimal("250.00"))
                .build();
        pedido.addItem(item);

        PedidoResponseDTO response = PedidoMapper.toResponseDTO(pedido);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(7L, response.getClienteId());
        assertEquals(EstadoPedido.CREADO, response.getEstado());
        assertEquals(new BigDecimal("500.00"), response.getMontoTotal());
        assertEquals(now, response.getFechaCreacion());
        assertEquals(now, response.getFechaActualizacion());
        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getItems().get(0).getId());
        assertEquals(100L, response.getItems().get(0).getProductoId());
        assertEquals(2, response.getItems().get(0).getCantidad());
        assertEquals(new BigDecimal("500.00"), response.getItems().get(0).getSubtotal());
    }

    @Test
    void testToDTO() {
        LocalDateTime now = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .id(1L)
                .clienteId(10L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("1500.50"))
                .fechaCreacion(now)
                .fechaActualizacion(now)
                .build();

        ItemPedido item = ItemPedido.builder()
                .id(100L)
                .productoId(5L)
                .cantidad(2)
                .precioUnitario(new BigDecimal("750.25"))
                .build();
        pedido.addItem(item);

        PedidoDTO dto = PedidoMapper.toDTO(pedido);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getClienteId());
        assertEquals(EstadoPedido.CREADO, dto.getEstado());
        assertEquals(new BigDecimal("1500.50"), dto.getMontoTotal());
        assertEquals(now, dto.getFechaCreacion());
        assertEquals(now, dto.getFechaActualizacion());
        assertEquals(1, dto.getItems().size());
        assertEquals(5L, dto.getItems().get(0).getProductoId());
        assertEquals(2, dto.getItems().get(0).getCantidad());
    }

    @Test
    void testToEntity() {
        LocalDateTime now = LocalDateTime.now();
        ItemPedidoDTO itemDTO = ItemPedidoDTO.builder()
                .id(200L)
                .productoId(8L)
                .cantidad(3)
                .precioUnitario(new BigDecimal("1000.00"))
                .build();

        PedidoDTO dto = PedidoDTO.builder()
                .id(2L)
                .clienteId(20L)
                .estado(EstadoPedido.CONFIRMADO)
                .montoTotal(new BigDecimal("3000.00"))
                .fechaCreacion(now)
                .fechaActualizacion(now)
                .items(List.of(itemDTO))
                .build();

        Pedido entity = PedidoMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(2L, entity.getId());
        assertEquals(20L, entity.getClienteId());
        assertEquals(EstadoPedido.CONFIRMADO, entity.getEstado());
        assertEquals(new BigDecimal("3000.00"), entity.getMontoTotal());
        assertEquals(now, entity.getFechaCreacion());
        assertEquals(now, entity.getFechaActualizacion());
        assertEquals(1, entity.getItems().size());
        assertEquals(8L, entity.getItems().get(0).getProductoId());
        assertEquals(entity, entity.getItems().get(0).getPedido());
    }
}
