package com.fube.pedidos.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.ItemPedidoRequestDTO;
import com.fube.pedidos.dto.ItemPedidoResponseDTO;
import com.fube.pedidos.dto.PedidoResponseDTO;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.servicios.PedidoServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoControlador.class)
class PedidoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoServicio pedidoServicio;

    @Test
    void testCrear_Retorna201() throws Exception {
        CrearPedidoRequestDTO request = CrearPedidoRequestDTO.builder()
                .clienteId(1L)
                .items(List.of(new ItemPedidoRequestDTO(10L, 2, new BigDecimal("100.00"))))
                .build();

        PedidoResponseDTO response = PedidoResponseDTO.builder()
                .id(1L)
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("200.00"))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .items(List.of(new ItemPedidoResponseDTO(1L, 10L, 2, new BigDecimal("100.00"), new BigDecimal("200.00"))))
                .build();

        when(pedidoServicio.crearPedido(any(CrearPedidoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.clienteId").value(1L))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$.montoTotal").value(200.00));
    }

    @Test
    void testBuscarPorId_Retorna200() throws Exception {
        PedidoResponseDTO response = PedidoResponseDTO.builder()
                .id(2L)
                .clienteId(5L)
                .estado(EstadoPedido.CONFIRMADO)
                .montoTotal(new BigDecimal("1500.00"))
                .build();

        when(pedidoServicio.buscarPorId(2L)).thenReturn(response);

        mockMvc.perform(get("/api/pedidos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.clienteId").value(5L))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void testBuscarPorClienteId_Retorna200() throws Exception {
        PedidoResponseDTO p1 = PedidoResponseDTO.builder().id(10L).clienteId(8L).build();
        PedidoResponseDTO p2 = PedidoResponseDTO.builder().id(11L).clienteId(8L).build();

        when(pedidoServicio.buscarPorClienteId(8L)).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/pedidos/cliente/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[1].id").value(11L));
    }

    @Test
    void testCancelar_Retorna200() throws Exception {
        PedidoResponseDTO response = PedidoResponseDTO.builder()
                .id(3L)
                .clienteId(5L)
                .estado(EstadoPedido.CANCELADO)
                .montoTotal(new BigDecimal("500.00"))
                .build();

        when(pedidoServicio.cancelarPedido(eq(3L))).thenReturn(response);

        mockMvc.perform(put("/api/pedidos/3/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }
}
