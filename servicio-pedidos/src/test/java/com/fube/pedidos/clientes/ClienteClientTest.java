package com.fube.pedidos.clientes;

import com.fube.pedidos.clientes.dto.ClienteResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClienteClientTest {

    private ClienteClient clienteClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        clienteClient = new ClienteClient(builder, "http://localhost:8080");
    }

    @Test
    void testExisteCliente_CuandoExiste() {
        mockServer.expect(requestTo("http://localhost:8080/api/clientes/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1,\"nombre\":\"Juan\"}", MediaType.APPLICATION_JSON));

        boolean existe = clienteClient.existeCliente(1L);

        assertTrue(existe);
        mockServer.verify();
    }

    @Test
    void testExisteCliente_CuandoNoExiste_RetornaFalse() {
        mockServer.expect(requestTo("http://localhost:8080/api/clientes/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        boolean existe = clienteClient.existeCliente(999L);

        assertFalse(existe);
        mockServer.verify();
    }

    @Test
    void testObtenerCliente_CuandoExiste() {
        mockServer.expect(requestTo("http://localhost:8080/api/clientes/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":2,\"nombre\":\"Maria\",\"email\":\"maria@mail.com\"}", MediaType.APPLICATION_JSON));

        Optional<ClienteResponseDTO> cliente = clienteClient.obtenerCliente(2L);

        assertTrue(cliente.isPresent());
        assertEquals(2L, cliente.get().getId());
        assertEquals("Maria", cliente.get().getNombre());
        assertEquals("maria@mail.com", cliente.get().getEmail());
        mockServer.verify();
    }
}
