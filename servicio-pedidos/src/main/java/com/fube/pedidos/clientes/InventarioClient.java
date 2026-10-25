package com.fube.pedidos.clientes;

import com.fube.pedidos.clientes.dto.ItemStockDTO;
import com.fube.pedidos.clientes.dto.ReservaStockRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class InventarioClient {

    private final RestClient restClient;

    public InventarioClient(RestClient.Builder builder,
                            @Value("${inventario.url:http://localhost:8081}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Llama al microservicio de inventario (puerto 8081) para reservar stock.
     *
     * @param request Datos de la reserva (pedidoId y lista de items con productoId y cantidad)
     * @return true si la reserva fue exitosa (HTTP 2xx), false si no hay stock disponible (HTTP 400 / 409)
     */
    public boolean reservarStock(ReservaStockRequestDTO request) {
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

    /**
     * Sobrecarga para reservar stock pasando directamente pedidoId y lista de items.
     *
     * @param pedidoId ID del pedido
     * @param items    Lista de items con productoId y cantidad
     * @return true si la reserva fue exitosa
     */
    public boolean reservarStock(Long pedidoId, List<ItemStockDTO> items) {
        return reservarStock(ReservaStockRequestDTO.builder()
                .pedidoId(pedidoId)
                .items(items)
                .build());
    }

    /**
     * Llama al microservicio de inventario (puerto 8081) para liberar el stock reservado de un pedido.
     *
     * @param request Datos de la liberación de stock (pedidoId y lista de items)
     * @return true si la liberación fue exitosa (HTTP 2xx)
     */
    public boolean liberarStock(ReservaStockRequestDTO request) {
        try {
            return Boolean.TRUE.equals(restClient.post()
                    .uri("/api/inventario/liberar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((req, response) -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return true;
                        } else {
                            throw new RuntimeException("Respuesta inesperada al liberar stock: " + response.getStatusCode());
                        }
                    }));
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con servicio-inventario al liberar stock: " + e.getMessage(), e);
        }
    }

    /**
     * Sobrecarga para liberar stock pasando directamente pedidoId y lista de items.
     *
     * @param pedidoId ID del pedido
     * @param items    Lista de items con productoId y cantidad
     * @return true si la liberación fue exitosa
     */
    public boolean liberarStock(Long pedidoId, List<ItemStockDTO> items) {
        return liberarStock(ReservaStockRequestDTO.builder()
                .pedidoId(pedidoId)
                .items(items)
                .build());
    }

    /**
     * Reserva stock para un producto individual.
     *
     * @param productoId ID del producto
     * @param cantidad   Cantidad a reservar
     * @return true si la reserva fue exitosa
     */
    public boolean reservarStockItem(Long productoId, Integer cantidad) {
        return reservarStock(null, List.of(new ItemStockDTO(productoId, cantidad)));
    }

    /**
     * Libera stock para un producto individual.
     *
     * @param productoId ID del producto
     * @param cantidad   Cantidad a liberar
     * @return true si la liberación fue exitosa
     */
    public boolean liberarStockItem(Long productoId, Integer cantidad) {
        return liberarStock(null, List.of(new ItemStockDTO(productoId, cantidad)));
    }

    /**
     * Libera la reserva de stock asociada a un pedido.
     *
     * @param pedidoId ID del pedido cuya reserva se desea liberar
     * @param items    Lista de items asociados a la reserva
     * @return true si la liberación fue exitosa
     */
    public boolean liberarReserva(Long pedidoId, List<ItemStockDTO> items) {
        return liberarStock(pedidoId, items);
    }

    /**
     * Libera la reserva de stock asociada a un pedido por su ID.
     *
     * @param pedidoId ID del pedido
     * @return true si la liberación fue exitosa
     */
    public boolean liberarReserva(Long pedidoId) {
        return liberarStock(pedidoId, List.of());
    }

    /**
     * Libera la reserva de stock a partir del DTO de reserva.
     *
     * @param request Datos de la reserva
     * @return true si la liberación fue exitosa
     */
    public boolean liberarReserva(ReservaStockRequestDTO request) {
        return liberarStock(request);
    }
}
