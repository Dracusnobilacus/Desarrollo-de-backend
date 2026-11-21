package com.fube.inventario.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fube.inventario.dto.ProductoDTO;
import com.fube.inventario.dto.ReservaRequestDTO;
import com.fube.inventario.dto.ReservaResponseDTO;
import com.fube.inventario.excepciones.GlobalExceptionHandler;
import com.fube.inventario.excepciones.RecursoNoEncontradoException;
import com.fube.inventario.excepciones.StockInsuficienteException;
import com.fube.inventario.modelos.EstadoReserva;
import com.fube.inventario.servicios.InventarioServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioControlador.class)
@Import(GlobalExceptionHandler.class)
class InventarioControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventarioServicio servicio;

    @Test
    @DisplayName("GET /api/inventario/productos retorna 200 y la lista de productos")
    void listarProductos_retornaOk() throws Exception {
        when(servicio.listarProductos()).thenReturn(List.of(new ProductoDTO(1L, "Mouse", 15)));

        mockMvc.perform(get("/api/inventario/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Mouse"))
                .andExpect(jsonPath("$[0].cantidadDisponible").value(15));
    }

    @Test
    @DisplayName("GET /api/inventario/productos/{id}/disponibilidad retorna 404 cuando el producto no existe")
    void consultarDisponibilidad_cuandoNoExiste_retornaNotFound() throws Exception {
        when(servicio.consultarDisponibilidad(99L))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado con ID: 99"));

        mockMvc.perform(get("/api/inventario/productos/{id}/disponibilidad", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/inventario/reservas retorna 201 CREATED cuando hay stock")
    void crearReserva_conStock_retornaCreated() throws Exception {
        ReservaRequestDTO request = new ReservaRequestDTO(1L, 10L, 2);
        ReservaResponseDTO response = new ReservaResponseDTO(5L, 1L, "Mouse", 10L, 2, EstadoReserva.ACTIVA);

        when(servicio.crearReserva(any(ReservaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventario/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("POST /api/inventario/reservas retorna 409 CONFLICT cuando falta stock")
    void crearReserva_sinStock_retornaConflict() throws Exception {
        ReservaRequestDTO request = new ReservaRequestDTO(1L, 10L, 999);
        when(servicio.crearReserva(any(ReservaRequestDTO.class)))
                .thenThrow(new StockInsuficienteException("Stock insuficiente"));

        mockMvc.perform(post("/api/inventario/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DELETE /api/inventario/reservas/{id} retorna 200 OK")
    void liberarReserva_retornaOk() throws Exception {
        ReservaResponseDTO liberada = new ReservaResponseDTO(5L, 1L, "Mouse", 10L, 2, EstadoReserva.LIBERADA);
        when(servicio.liberarReserva(5L)).thenReturn(liberada);

        mockMvc.perform(delete("/api/inventario/reservas/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LIBERADA"));
    }

    @Test
    @DisplayName("DELETE /api/inventario/pedidos/{pedidoId}/reservas retorna 200 OK")
    void liberarReservasPorPedido_retornaOk() throws Exception {
        ReservaResponseDTO liberada = new ReservaResponseDTO(5L, 1L, "Mouse", 10L, 2, EstadoReserva.LIBERADA);
        when(servicio.liberarReservasPorPedido(10L)).thenReturn(List.of(liberada));

        mockMvc.perform(delete("/api/inventario/pedidos/{pedidoId}/reservas", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("LIBERADA"));
    }
}
