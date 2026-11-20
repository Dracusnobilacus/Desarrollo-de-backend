package com.fube.pedidos.clientes;

import com.fube.pedidos.clientes.dto.ClienteResponseDTO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Component
public class ClienteClient {

    private final RestClient restClient;

    public ClienteClient(RestClient.Builder builder,
            @Value("${clientes.url:http://localhost:8080}") String baseUrl) {
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
     * Valida si existe un cliente por su ID en el microservicio de clientes (puerto 8080).
     */
    public boolean existeCliente(Long clienteId) {
        if (clienteId == null) {
            return false;
        }

        try {
            restClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return false;
            }
            throw new RuntimeException("Error al consultar servicio-clientes (HTTP "
                    + e.getStatusCode().value() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con servicio-clientes en puerto 8080: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los datos del cliente desde el microservicio de clientes.
     */
    public Optional<ClienteResponseDTO> obtenerCliente(Long clienteId) {
        if (clienteId == null) {
            return Optional.empty();
        }

        try {
            ClienteResponseDTO cliente = restClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        // Evita que lance excepción en 404 para retornar empty
                    })
                    .body(ClienteResponseDTO.class);

            return Optional.ofNullable(cliente);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("Error al obtener cliente: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con servicio-clientes: " + e.getMessage(), e);
        }
    }
}
