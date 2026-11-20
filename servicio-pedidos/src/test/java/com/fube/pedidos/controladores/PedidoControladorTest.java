package com.fube.pedidos.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fube.pedidos.dto.ActualizarPedidoRequestDTO;
import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.ItemPedidoDTO;
import com.fube.pedidos.dto.ItemPedidoRequestDTO;
import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.excepciones.GlobalExceptionHandler;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.servicios.PedidoServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoControlador.class)
@Import(GlobalExceptionHandler.class)
class PedidoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoServicio servicio;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean
    private Job expirarPedidosJob;

    @Test
    @DisplayName("POST /api/pedidos con CrearPedidoRequestDTO válido retorna 201 y crea entidad sin IDs")
    void crear_conDatosValidos_retornaCreated() throws Exception {
        CrearPedidoRequestDTO request = CrearPedidoRequestDTO.builder()
                .clienteId(1L)
                .items(List.of(ItemPedidoRequestDTO.builder()
                        .productoId(2L)
                        .cantidad(3)
                        .precioUnitario(new BigDecimal("500.00"))
                        .build()))
                .build();

        PedidoDTO respuesta = PedidoDTO.builder()
                .id(10L)
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("1500.00"))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .items(List.of(ItemPedidoDTO.builder()
                        .id(20L)
                        .productoId(2L)
                        .cantidad(3)
                        .precioUnitario(new BigDecimal("500.00"))
                        .subtotal(new BigDecimal("1500.00"))
                        .build()))
                .build();

        when(servicio.crear(any(Pedido.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$.montoTotal").value(1500.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(1500.00));

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(servicio).crear(captor.capture());
        Pedido enviado = captor.getValue();
        assertNull(enviado.getId());
        assertEquals(1L, enviado.getClienteId());
        assertEquals(1, enviado.getItems().size());
        assertNull(enviado.getItems().get(0).getId());
        assertEquals(new BigDecimal("1500.00"), enviado.getMontoTotal());
    }

    @Test
    @DisplayName("POST /api/pedidos con campos inválidos retorna 400 BAD REQUEST")
    void crear_conDatosInvalidos_retornaBadRequest() throws Exception {
        CrearPedidoRequestDTO requestInvalido = CrearPedidoRequestDTO.builder()
                .clienteId(0L)
                .items(List.of())
                .build();

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        verifyNoInteractions(servicio);
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id} usa ActualizarPedidoRequestDTO sin id en body y toma el id del path")
    void actualizar_conDatosValidos_tomaIdDelPath() throws Exception {
        ActualizarPedidoRequestDTO request = ActualizarPedidoRequestDTO.builder()
                .clienteId(2L)
                .estado(EstadoPedido.CREADO)
                .items(List.of(ItemPedidoRequestDTO.builder()
                        .productoId(5L)
                        .cantidad(2)
                        .precioUnitario(new BigDecimal("250.00"))
                        .build()))
                .build();

        PedidoDTO respuesta = PedidoDTO.builder()
                .id(7L)
                .clienteId(2L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("500.00"))
                .build();

        when(servicio.actualizar(any(Pedido.class))).thenReturn(respuesta);

        mockMvc.perform(put("/api/pedidos/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.clienteId").value(2L));

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(servicio).actualizar(captor.capture());
        Pedido enviado = captor.getValue();
        assertEquals(7L, enviado.getId());
        assertEquals(2L, enviado.getClienteId());
        assertEquals(1, enviado.getItems().size());
        assertNull(enviado.getItems().get(0).getId());
    }
}
