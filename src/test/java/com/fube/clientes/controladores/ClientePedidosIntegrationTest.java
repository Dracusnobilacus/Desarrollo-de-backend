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
                        "descripcion": "Teclado mecanico",
                        "total": 45.99,
                        "estado": "COMPLETADO"
                    },
                    {
                        "id": 102,
                        "clienteId": %d,
                        "descripcion": "Mouse inalambrico",
                        "total": 19.50,
                        "estado": "ENVIADO"
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
                .andExpect(jsonPath("$[0].descripcion").value("Teclado mecanico"))
                .andExpect(jsonPath("$[0].total").value(45.99))
                .andExpect(jsonPath("$[0].estado").value("COMPLETADO"))
                .andExpect(jsonPath("$[1].id").value(102))
                .andExpect(jsonPath("$[1].descripcion").value("Mouse inalambrico"));

        // Verificamos que se haya invocado a MockServer exactamente 1 vez
        mockServer.verify(
                request()
                        .withMethod("GET")
                        .withPath("/api/pedidos/cliente/" + clienteId),
                VerificationTimes.once()
        );
    }
}
