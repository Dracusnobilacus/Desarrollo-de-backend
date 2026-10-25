package com.fube.pedidos.servicios;

import com.fube.pedidos.clientes.ClienteClient;
import com.fube.pedidos.clientes.InventarioClient;
import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.ItemPedidoRequestDTO;
import com.fube.pedidos.dto.PedidoResponseDTO;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServicioTest {

    @Mock
    private PedidoRepositorio repositorio;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private InventarioClient inventarioClient;

    private PedidoServicioImpl pedidoServicio;

    @BeforeEach
    void setUp() {
        pedidoServicio = new PedidoServicioImpl(repositorio, clienteClient, inventarioClient);
    }

    @Test
    void testCrearPedido_Exitoso() {
        Long clienteId = 10L;
        ItemPedidoRequestDTO item1 = new ItemPedidoRequestDTO(101L, 2, new BigDecimal("500.00"));
        ItemPedidoRequestDTO item2 = new ItemPedidoRequestDTO(102L, 1, new BigDecimal("1000.00"));
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO(clienteId, List.of(item1, item2));

        // 1) Cliente existe
        when(clienteClient.existeCliente(clienteId)).thenReturn(true);

        // 2) Stock disponible para cada ítem
        when(inventarioClient.reservarStockItem(101L, 2)).thenReturn(true);
        when(inventarioClient.reservarStockItem(102L, 1)).thenReturn(true);

        // 3) Guardar en repositorio
        when(repositorio.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PedidoResponseDTO response = pedidoServicio.crearPedido(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(clienteId, response.getClienteId());
        assertEquals(EstadoPedido.CREADO, response.getEstado());
        assertEquals(new BigDecimal("2000.00"), response.getMontoTotal());
        assertEquals(2, response.getItems().size());

        verify(clienteClient).existeCliente(clienteId);
        verify(inventarioClient).reservarStockItem(101L, 2);
        verify(inventarioClient).reservarStockItem(102L, 1);
        verify(repositorio).save(any(Pedido.class));
        verify(inventarioClient, never()).liberarStockItem(any(), any());
    }

    @Test
    void testCrearPedido_ClienteNoExiste_Aborta() {
        Long clienteId = 999L;
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO(clienteId,
                List.of(new ItemPedidoRequestDTO(101L, 1, new BigDecimal("100.00"))));

        when(clienteClient.existeCliente(clienteId)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoServicio.crearPedido(request));
        assertTrue(ex.getMessage().contains("no existe"));

        verify(clienteClient).existeCliente(clienteId);
        verifyNoInteractions(inventarioClient);
        verifyNoInteractions(repositorio);
    }

    @Test
    void testCrearPedido_StockInsuficiente_AbortaYLiberaReservasPrevias() {
        Long clienteId = 10L;
        ItemPedidoRequestDTO item1 = new ItemPedidoRequestDTO(101L, 2, new BigDecimal("500.00"));
        ItemPedidoRequestDTO item2 = new ItemPedidoRequestDTO(102L, 10, new BigDecimal("1000.00"));
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO(clienteId, List.of(item1, item2));

        when(clienteClient.existeCliente(clienteId)).thenReturn(true);
        when(inventarioClient.reservarStockItem(101L, 2)).thenReturn(true);
        when(inventarioClient.reservarStockItem(102L, 10)).thenReturn(false); // Falla reserva de item 2

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoServicio.crearPedido(request));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));

        // Verifica que se liberó el stock del primer ítem reservado
        verify(inventarioClient).liberarStockItem(101L, 2);
        verify(repositorio, never()).save(any());
    }

    @Test
    void testCrearPedido_ErrorAlGuardar_AbortaYLiberaTodoElStock() {
        Long clienteId = 10L;
        ItemPedidoRequestDTO item1 = new ItemPedidoRequestDTO(101L, 2, new BigDecimal("500.00"));
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO(clienteId, List.of(item1));

        when(clienteClient.existeCliente(clienteId)).thenReturn(true);
        when(inventarioClient.reservarStockItem(101L, 2)).thenReturn(true);
        when(repositorio.save(any(Pedido.class))).thenThrow(new RuntimeException("Error en base de datos"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoServicio.crearPedido(request));
        assertTrue(ex.getMessage().contains("Error al crear pedido"));

        // Verifica que se liberó el stock reservado
        verify(inventarioClient).liberarStockItem(101L, 2);
    }

    @Test
    void testCancelarPedido_EstadoCreado_Exitoso() {
        Long pedidoId = 1L;
        com.fube.pedidos.modelos.ItemPedido item = com.fube.pedidos.modelos.ItemPedido.builder()
                .id(10L)
                .productoId(50L)
                .cantidad(3)
                .precioUnitario(new BigDecimal("200.00"))
                .build();

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteId(5L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("600.00"))
                .build();
        pedido.addItem(item);

        when(repositorio.findById(pedidoId)).thenReturn(java.util.Optional.of(pedido));
        when(repositorio.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoServicio.cancelarPedido(pedidoId);

        assertNotNull(response);
        assertEquals(EstadoPedido.CANCELADO, response.getEstado());
        verify(inventarioClient).liberarReserva(eq(pedidoId), anyList());
        verify(repositorio).save(pedido);
    }

    @Test
    void testCancelarPedido_EstadoConfirmado_Exitoso() {
        Long pedidoId = 2L;
        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteId(5L)
                .estado(EstadoPedido.CONFIRMADO)
                .montoTotal(new BigDecimal("1200.00"))
                .build();

        when(repositorio.findById(pedidoId)).thenReturn(java.util.Optional.of(pedido));
        when(repositorio.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoServicio.cancelarPedido(pedidoId);

        assertNotNull(response);
        assertEquals(EstadoPedido.CANCELADO, response.getEstado());
        verify(inventarioClient).liberarReserva(eq(pedidoId), anyList());
        verify(repositorio).save(pedido);
    }

    @Test
    void testCancelarPedido_EstadoInvalido_LanzaExcepcion() {
        Long pedidoId = 3L;
        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteId(5L)
                .estado(EstadoPedido.EXPIRADO)
                .build();

        when(repositorio.findById(pedidoId)).thenReturn(java.util.Optional.of(pedido));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> pedidoServicio.cancelarPedido(pedidoId));
        assertTrue(ex.getMessage().contains("EXPIRADO"));

        verify(inventarioClient, never()).liberarReserva(any(), any());
        verify(repositorio, never()).save(any());
    }

    @Test
    void testCancelarPedido_NoEncontrado_LanzaExcepcion() {
        Long pedidoId = 99L;
        when(repositorio.findById(pedidoId)).thenReturn(java.util.Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoServicio.cancelarPedido(pedidoId));
        assertTrue(ex.getMessage().contains("no encontrado"));

        verifyNoInteractions(inventarioClient);
    }
}
