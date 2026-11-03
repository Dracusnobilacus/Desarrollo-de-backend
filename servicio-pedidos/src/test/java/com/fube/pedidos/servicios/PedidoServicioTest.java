package com.fube.pedidos.servicios;

import com.fube.pedidos.clientes.ClienteClient;
import com.fube.pedidos.clientes.InventarioClient;
import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.excepciones.RecursoNoEncontradoException;
import com.fube.pedidos.excepciones.StockInsuficienteException;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.ItemPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServicioTest {

    @Mock
    private PedidoRepositorio repositorio;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoServicioImpl servicio;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setClienteId(10L);
        pedido.setEstado(EstadoPedido.CREADO);

        ItemPedido item = ItemPedido.builder()
                .id(1L)
                .productoId(5L)
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .pedido(pedido)
                .build();

        pedido.setItems(new ArrayList<>(List.of(item)));
    }

    @Test
    @DisplayName("Crear pedido exitoso valida cliente, reserva stock y guarda el pedido")
    void crearPedido_exitoso() {
        when(clienteClient.existeCliente(10L)).thenReturn(true);
        when(repositorio.save(any(Pedido.class))).thenReturn(pedido);
        when(inventarioClient.reservarItem(eq(1L), eq(5L), eq(2))).thenReturn(true);

        PedidoDTO resultado = servicio.crear(pedido);

        assertNotNull(resultado);
        assertEquals(EstadoPedido.CREADO, resultado.getEstado());
        verify(clienteClient).existeCliente(10L);
        verify(inventarioClient).reservarItem(1L, 5L, 2);
        verify(repositorio, atLeastOnce()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Crear pedido con cliente inexistente lanza excepción y no reserva stock")
    void crearPedido_clienteInexistente_lanzaExcepcion() {
        when(clienteClient.existeCliente(10L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.crear(pedido));

        verify(clienteClient).existeCliente(10L);
        verifyNoInteractions(inventarioClient);
        verify(repositorio, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Crear pedido sin stock suficiente lanza excepción y libera reservas")
    void crearPedido_stockInsuficiente_lanzaExcepcion() {
        when(clienteClient.existeCliente(10L)).thenReturn(true);
        when(repositorio.save(any(Pedido.class))).thenReturn(pedido);
        when(inventarioClient.reservarItem(eq(1L), eq(5L), eq(2))).thenReturn(false);

        assertThrows(StockInsuficienteException.class, () -> servicio.crear(pedido));

        verify(inventarioClient).liberarReservasPorPedido(1L);
        verify(repositorio).delete(pedido);
    }

    @Test
    @DisplayName("Cancelar pedido en estado CREADO libera stock en inventario")
    void cancelarPedido_exitoso() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(pedido));
        when(repositorio.save(any(Pedido.class))).thenReturn(pedido);

        PedidoDTO resultado = servicio.cancelar(1L);

        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());
        verify(inventarioClient).liberarReservasPorPedido(1L);
        verify(repositorio).save(pedido);
    }

    @Test
    @DisplayName("Cancelar pedido ya cancelado lanza IllegalStateException")
    void cancelarPedido_estadoInvalido_lanzaExcepcion() {
        pedido.setEstado(EstadoPedido.CANCELADO);
        when(repositorio.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> servicio.cancelar(1L));

        verify(inventarioClient, never()).liberarReservasPorPedido(anyLong());
    }
}

