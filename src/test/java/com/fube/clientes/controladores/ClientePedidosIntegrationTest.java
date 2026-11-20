package com.fube.clientes.controladores;

import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClientePedidosIntegrationTest {

    private static ClientAndServer mockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    private Long clienteId;

    @BeforeAll
    static void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(0);
    }

    @AfterAll
    static void stopMockServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("pedidos.service.url", () -> "http://localhost:" + mockServer.getPort());
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();

        Cliente cliente = clienteRepositorio.findAll().stream().findFirst().orElseGet(() ->
                clienteRepositorio.save(Cliente.builder()
                        .nombre("Test")
                        .apellido("Integracion")
                        .email("test.integracion@example.com")
                        .build())
        );
        clienteId = cliente.getId();
    }

    @Test
    @DisplayName("GET /api/clientes/{id}/pedidos consulta servicio-pedidos simulado por MockServer")
    void buscarPedidosPorCliente_conMockServer_devuelveListaDePedidos() throws Exception {
        // GIVEN: MockServer simula la respuesta de servicio-pedidos
        String mockResponseJson = """
                [
                    {
                        "id": 101,
                        "clienteId": %d,
                        "estado": "CONFIRMADO",
                        "montoTotal": 45.99,
                        "fechaCreacion": "2026-11-20T18:00:00",
                        "fechaActualizacion": "2026-11-20T18:05:00",
                        "items": [
                            {
                                "id": 1,
                                "productoId": 10,
                                "cantidad": 1,
                                "precioUnitario": 45.99,
                                "subtotal": 45.99
                            }
                        ]
                    },
                    {
                        "id": 102,
                        "clienteId": %d,
                        "estado": "CREADO",
                        "montoTotal": 19.50,
                        "fechaCreacion": "2026-11-20T19:00:00",
                        "fechaActualizacion": "2026-11-20T19:00:00",
                        "items": [
                            {
                                "id": 2,
                                "productoId": 11,
                                "cantidad": 2,
                                "precioUnitario": 9.75,
                                "subtotal": 19.50
                            }
                        ]
                    }
                ]
                """.formatted(clienteId, clienteId);

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/pedidos/cliente/" + clienteId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(mockResponseJson)
                );

        // WHEN & THEN: consultamos a nuestro endpoint en servicio-clientes
        mockMvc.perform(get("/api/clientes/{id}/pedidos", clienteId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].clienteId").value(clienteId))
                .andExpect(jsonPath("$[0].estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$[0].montoTotal").value(45.99))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].productoId").value(10))
                .andExpect(jsonPath("$[1].id").value(102))
                .andExpect(jsonPath("$[1].estado").value("CREADO"))
                .andExpect(jsonPath("$[1].items[0].cantidad").value(2));

        // Verificamos que se haya invocado a MockServer exactamente 1 vez
        mockServer.verify(
                request()
                        .withMethod("GET")
                        .withPath("/api/pedidos/cliente/" + clienteId),
                VerificationTimes.once()
        );
    }
}
