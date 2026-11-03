package com.fube.pedidos.clientes;

import com.fube.pedidos.clientes.dto.ItemStockDTO;
import com.fube.pedidos.clientes.dto.ReservaStockRequestDTO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
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

    /**
     * Llama al microservicio de inventario para reservar stock en lote.
     */
    public boolean reservarStock(ReservaStockRequestDTO request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(restClient.post()
                    .uri("/api/inventario/reservar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((req, response) -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return true;
                        } else if (response.getStatusCode().value() == 400 || response.getStatusCode().value() == 409) {
                            return false;
                        } else {
                            throw new RuntimeException("Respuesta inesperada al reservar stock: " + response.getStatusCode());
                        }
                    }));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 409) {
                return false;
            }
            throw new RuntimeException("Error en servicio de inventario al reservar: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con servicio-inventario en puerto 8081: " + e.getMessage(), e);
        }
    }

    public boolean reservarStock(Long pedidoId, List<ItemStockDTO> items) {
        return reservarStock(ReservaStockRequestDTO.builder()
                .pedidoId(pedidoId)
                .items(items)
                .build());
    }

    public boolean liberarStock(ReservaStockRequestDTO request) {
        try {
            return Boolean.TRUE.equals(restClient.post()
                    .uri("/api/inventario/liberar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((req, response) -> response.getStatusCode().is2xxSuccessful()));
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con servicio-inventario al liberar stock: " + e.getMessage(), e);
        }
    }

    public boolean liberarStock(Long pedidoId, List<ItemStockDTO> items) {
        return liberarStock(ReservaStockRequestDTO.builder()
                .pedidoId(pedidoId)
                .items(items)
                .build());
    }
}
