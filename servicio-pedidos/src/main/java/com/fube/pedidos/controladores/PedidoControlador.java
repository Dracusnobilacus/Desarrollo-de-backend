package com.fube.pedidos.controladores;

import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.PedidoResponseDTO;
import com.fube.pedidos.servicios.PedidoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Operaciones y ciclo de vida de pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;

    public PedidoControlador(PedidoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @Operation(
            summary = "Crear un nuevo pedido",
            description = "Valida la existencia del cliente en servicio-clientes, reserva el stock de cada ítem en servicio-inventario, calcula el monto total y persiste el pedido en estado CREADO. Si algo falla, la operación se aborta."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida, cliente no encontrado o datos incorrectos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Stock insuficiente para uno o más ítems del pedido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor o falla de comunicación con microservicios",
                    content = @Content
            )
    })
    public ResponseEntity<PedidoResponseDTO> crear(
            @RequestBody
            @Parameter(description = "Datos del pedido a crear (clienteId y lista de ítems)", required = true)
            CrearPedidoRequestDTO request) {
        PedidoResponseDTO creado = servicio.crearPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener pedido por ID",
            description = "Devuelve el detalle completo del pedido, sus ítems asociados, estado y montos totales por su identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado para el ID indicado",
                    content = @Content
            )
    })
    public ResponseEntity<PedidoResponseDTO> buscarPorId(
            @PathVariable
            @Parameter(description = "Identificador único del pedido", example = "1", required = true)
            Long id) {
        return ResponseEntity.ok(servicio.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(
            summary = "Listar pedidos de un cliente",
            description = "Obtiene la lista de todos los pedidos registrados pertenecientes a un cliente determinado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos obtenida con éxito",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PedidoResponseDTO.class)))
            )
    })
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorClienteId(
            @PathVariable
            @Parameter(description = "Identificador único del cliente", example = "10", required = true)
            Long clienteId) {
        return ResponseEntity.ok(servicio.buscarPorClienteId(clienteId));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(
            summary = "Cancelar pedido",
            description = "Cancela un pedido existente si se encuentra en estado CREADO o CONFIRMADO, cambiando su estado a CANCELADO y liberando el stock previamente reservado en servicio-inventario."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido cancelado con éxito y reserva de stock liberada",
                    content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El pedido no se encuentra en estado CREADO o CONFIRMADO para poder cancelarlo",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado para el ID indicado",
                    content = @Content
            )
    })
    public ResponseEntity<PedidoResponseDTO> cancelar(
            @PathVariable
            @Parameter(description = "Identificador único del pedido a cancelar", example = "1", required = true)
            Long id) {
        return ResponseEntity.ok(servicio.cancelarPedido(id));
    }
}
