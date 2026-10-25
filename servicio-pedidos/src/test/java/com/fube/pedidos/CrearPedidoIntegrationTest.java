package com.fube.pedidos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.ItemPedidoRequestDTO;
import com.fube.pedidos.dto.PedidoResponseDTO;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import com.fube.pedidos.servicios.PedidoServicio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CrearPedidoIntegrationTest {

    private static ClientAndServer mockServerClientes;
    private static ClientAndServer mockServerInventario;

    @Autowired
    private PedidoServicio pedidoServicio;

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void startMockServers() {
        mockServerClientes = ClientAndServer.startClientAndServer();
        mockServerInventario = ClientAndServer.startClientAndServer();
    }

    @AfterAll
    static void stopMockServers() {
        if (mockServerClientes != null) {
            mockServerClientes.stop();
        }
        if (mockServerInventario != null) {
            mockServerInventario.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("clientes.url", () -> "http://localhost:" + mockServerClientes.getPort());
        registry.add("inventario.url", () -> "http://localhost:" + mockServerInventario.getPort());
    }

    @BeforeEach
    void resetMockServers() {
        mockServerClientes.reset();
        mockServerInventario.reset();
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void testCrearPedido_CuandoClientesEInventarioResponden200_CreaPedidoCorrectamente() {
        Long clienteId = 1L;
        Long productoId = 101L;
        Integer cantidad = 3;
        BigDecimal precioUnitario = new BigDecimal("1250.00");
        BigDecimal montoTotalEsperado = new BigDecimal("3750.00");

        // 1. Simular respuesta 200 de servicio-clientes
        mockServerClientes
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/api/clientes/" + clienteId))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":" + clienteId + ",\"nombre\":\"Carlos\",\"apellido\":\"Gomez\",\"email\":\"carlos@test.com\"}"));

        // 2. Simular respuesta 200 de servicio-inventario para la reserva de stock
        mockServerInventario
                .when(HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/api/inventario/reservar"))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"OK\",\"mensaje\":\"Stock reservado con éxito\"}"));

        // 3. Crear solicitud
        ItemPedidoRequestDTO itemDto = ItemPedidoRequestDTO.builder()
                .productoId(productoId)
                .cantidad(cantidad)
                .precioUnitario(precioUnitario)
                .build();

        CrearPedidoRequestDTO request = CrearPedidoRequestDTO.builder()
                .clienteId(clienteId)
                .items(List.of(itemDto))
                .build();

        // 4. Ejecutar creación a través de PedidoServicio
        PedidoResponseDTO response = pedidoServicio.crearPedido(request);

        // 5. Validaciones de la respuesta
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(clienteId, response.getClienteId());
        assertEquals(EstadoPedido.CREADO, response.getEstado());
        assertEquals(montoTotalEsperado, response.getMontoTotal());
        assertNotNull(response.getFechaCreacion());
        assertNotNull(response.getFechaActualizacion());
        assertEquals(1, response.getItems().size());
        assertEquals(productoId, response.getItems().get(0).getProductoId());
        assertEquals(cantidad, response.getItems().get(0).getCantidad());
        assertEquals(precioUnitario, response.getItems().get(0).getPrecioUnitario());

        // 6. Validar persistencia en base de datos
        Pedido pedidoEnBD = pedidoRepositorio.findById(response.getId()).orElse(null);
        assertNotNull(pedidoEnBD);
        assertEquals(EstadoPedido.CREADO, pedidoEnBD.getEstado());
        assertEquals(montoTotalEsperado, pedidoEnBD.getMontoTotal());
        assertEquals(1, pedidoEnBD.getItems().size());

        // 7. Verificar que MockServer recibió las llamadas esperadas
        mockServerClientes.verify(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/api/clientes/" + clienteId),
                VerificationTimes.exactly(1)
        );

        mockServerInventario.verify(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/api/inventario/reservar"),
                VerificationTimes.exactly(1)
        );
    }

    @Test
    void testCrearPedido_ViaEndpointHttp_CuandoAmbosResponden200_Retorna201() throws Exception {
        Long clienteId = 2L;
        Long productoId = 205L;

        // Simular respuesta 200 de servicio-clientes
        mockServerClientes
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/api/clientes/" + clienteId))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":" + clienteId + ",\"nombre\":\"Laura\",\"apellido\":\"Lopez\"}"));

        // Simular respuesta 200 de servicio-inventario
        mockServerInventario
                .when(HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/api/inventario/reservar"))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"RESERVADO\"}"));

        CrearPedidoRequestDTO request = CrearPedidoRequestDTO.builder()
                .clienteId(clienteId)
                .items(List.of(new ItemPedidoRequestDTO(productoId, 2, new BigDecimal("800.00"))))
                .build();

        // Ejecutar llamada HTTP al controlador
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$.montoTotal").value(1600.00))
                .andExpect(jsonPath("$.items[0].productoId").value(productoId))
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }
}
