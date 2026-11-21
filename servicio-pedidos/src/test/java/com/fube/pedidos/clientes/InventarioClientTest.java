package com.fube.pedidos.clientes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class InventarioClientTest {

    private InventarioClient inventarioClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        inventarioClient = new InventarioClient(builder, "http://localhost:8081");
    }

    @Test
    void testReservarItem_Exitoso() {
        mockServer.expect(requestTo("http://localhost:8081/api/inventario/reservas"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":1,\"productoId\":10,\"pedidoId\":1,\"cantidad\":2,\"estado\":\"ACTIVA\"}"));

        boolean resultado = inventarioClient.reservarItem(1L, 10L, 2);

        assertTrue(resultado);
        mockServer.verify();
    }

    @Test
    void testReservarItem_SinStock_RetornaFalse() {
        mockServer.expect(requestTo("http://localhost:8081/api/inventario/reservas"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        boolean resultado = inventarioClient.reservarItem(1L, 10L, 500);

        assertFalse(resultado);
        mockServer.verify();
    }

    @Test
    void testLiberarReservasPorPedido_Exitoso() {
        mockServer.expect(requestTo("http://localhost:8081/api/inventario/pedidos/1/reservas"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        boolean resultado = inventarioClient.liberarReservasPorPedido(1L);

        assertTrue(resultado);
        mockServer.verify();
    }

    @Test
    void testLiberarReservasPorPedido_CuandoFalla_RetornaFalse() {
        mockServer.expect(requestTo("http://localhost:8081/api/inventario/pedidos/99/reservas"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        boolean resultado = inventarioClient.liberarReservasPorPedido(99L);

        assertFalse(resultado);
        mockServer.verify();
    }
}
