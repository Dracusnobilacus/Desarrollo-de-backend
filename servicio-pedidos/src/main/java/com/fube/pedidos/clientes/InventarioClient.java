package com.fube.pedidos.clientes;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class InventarioClient {

    private final RestClient restClient;

    public InventarioClient(RestClient.Builder builder,
                            @Value("${inventario.url:http://localhost:8081}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .requestInterceptor((req, body, execution) -> {
                    String traceId = MDC.get("traceId");
                    if (traceId != null && !traceId.isBlank()) {
                        req.getHeaders().add("X-Trace-Id", traceId);
                    }
                    return execution.execute(req, body);
                })
                .build();
    }

    /**
     * Reserva stock para un ítem específico de un pedido en servicio-inventario.
     */
    public boolean reservarItem(Long pedidoId, Long productoId, Integer cantidad) {
        if (pedidoId == null || productoId == null || cantidad == null || cantidad <= 0) {
            return false;
        }
        try {
            Map<String, Object> request = Map.of(
                    "pedidoId", pedidoId,
                    "productoId", productoId,
                    "cantidad", cantidad
            );
            return Boolean.TRUE.equals(restClient.post()
                    .uri("/api/inventario/reservas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((req, response) -> response.getStatusCode().is2xxSuccessful()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Libera todas las reservas de stock asociadas a un pedido en servicio-inventario.
     */
    public boolean liberarReservasPorPedido(Long pedidoId) {
        if (pedidoId == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(restClient.put()
                    .uri("/api/inventario/pedidos/{pedidoId}/liberar", pedidoId)
                    .exchange((req, response) -> response.getStatusCode().is2xxSuccessful()));
        } catch (Exception e) {
            return false;
        }
    }
}
