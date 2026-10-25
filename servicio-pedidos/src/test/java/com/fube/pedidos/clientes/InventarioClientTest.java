package com.fube.pedidos.clientes;

import com.fube.pedidos.clientes.dto.ItemStockDTO;
import com.fube.pedidos.clientes.dto.ReservaStockRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
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
    void testReservarStock_Exitoso() {
        mockServer.expect(requestTo("http://localhost:8081/api/inventario/reservar"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"mensaje\":\"Stock reservado\"}", MediaType.APPLICATION_JSON));

        List<ItemStockDTO> items = List.of(new ItemStockDTO(10L, 2));
        boolean resultado = inventarioClient.reservarStock(1L, items);

        assertTrue(resultado);
        mockServer.verify();
    }

    @Test
    void testReservarStock_SinStock_RetornaFalse() {
        mockServer.expect(requestTo("http://localhost:8081/api/inventario/reservar"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        List<ItemStockDTO> items = List.of(new ItemStockDTO(10L, 500));
        boolean resultado = inventarioClient.reservarStock(1L, items);

        assertFalse(resultado);
        mockServer.verify();
    }

    @Test
    void testLiberarStock_Exitoso() {
        mockServer.expect(requestTo("http://localhost:8081/api/inventario/liberar"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"mensaje\":\"Stock liberado\"}", MediaType.APPLICATION_JSON));

        ReservaStockRequestDTO request = ReservaStockRequestDTO.builder()
                .pedidoId(1L)
                .items(List.of(new ItemStockDTO(10L, 2)))
                .build();

        boolean resultado = inventarioClient.liberarStock(request);

        assertTrue(resultado);
        mockServer.verify();
    }
}
