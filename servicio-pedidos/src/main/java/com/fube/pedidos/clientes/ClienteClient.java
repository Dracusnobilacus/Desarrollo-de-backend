package com.fube.pedidos.clientes;

import com.fube.pedidos.clientes.dto.ClienteResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
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
                .build();
    }

    /**
     * Valida si existe un cliente por su ID en el microservicio de clientes (puerto 8080).
     *
     * @param clienteId ID del cliente a verificar
     * @return true si el cliente existe (HTTP 2xx), false si no existe (HTTP 404)
     */
    public boolean existeCliente(Long clienteId) {
        if (clienteId == null) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(restClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return true;
                        } else if (response.getStatusCode().value() == 404) {
                            return false;
                        } else {
                            throw new RuntimeException("Respuesta inesperada de servicio-clientes: " + response.getStatusCode());
                        }
                    }));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return false;
            }
            throw new RuntimeException("Error al consultar servicio-clientes: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con servicio-clientes en puerto 8080: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los datos del cliente desde el microservicio de clientes.
     *
     * @param clienteId ID del cliente
     * @return Optional con ClienteResponseDTO si existe, o Optional.empty() si no existe
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
