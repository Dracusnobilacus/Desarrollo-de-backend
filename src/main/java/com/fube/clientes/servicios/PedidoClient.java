package com.fube.clientes.servicios;

import com.fube.clientes.dto.PedidoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class PedidoClient {

    private final RestClient restClient;

    @Autowired
    public PedidoClient(RestClient.Builder restClientBuilder,
                        @Value("${pedidos.service.url:http://localhost:8082}") String pedidosUrl) {
        RestClient.Builder builder = restClientBuilder != null ? restClientBuilder : RestClient.builder();
        this.restClient = builder.baseUrl(pedidosUrl).build();
    }

    public PedidoClient(String pedidosUrl) {
        this(RestClient.builder(), pedidosUrl);
    }

    public List<PedidoDTO> obtenerPedidosPorCliente(Long clienteId) {
        List<PedidoDTO> pedidos = restClient.get()
                .uri("/api/pedidos/cliente/{id}", clienteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoDTO>>() {});
        return pedidos != null ? pedidos : Collections.emptyList();
    }
}
