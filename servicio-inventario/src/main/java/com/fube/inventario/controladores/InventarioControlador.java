package com.fube.inventario.controladores;

import com.fube.inventario.dto.ProductoDTO;
import com.fube.inventario.dto.ReservaRequestDTO;
import com.fube.inventario.dto.ReservaResponseDTO;
import com.fube.inventario.servicios.InventarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Endpoints para consulta de stock y gestión de reservas")
public class InventarioControlador {

    private final InventarioServicio servicio;

    public InventarioControlador(InventarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/productos")
    @Operation(summary = "Listar todos los productos")
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        return ResponseEntity.ok(servicio.listarProductos());
    }

    @GetMapping("/productos/{id}/disponibilidad")
    @Operation(summary = "Consultar disponibilidad de stock de un producto")
    public ResponseEntity<ProductoDTO> consultarDisponibilidad(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.consultarDisponibilidad(id));
    }

    @PostMapping("/reservas")
    @Operation(summary = "Reservar unidades de stock para un pedido")
    public ResponseEntity<ReservaResponseDTO> crearReserva(@RequestBody ReservaRequestDTO request) {
        ReservaResponseDTO respuesta = servicio.crearReserva(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/reservas/{id}/liberar")
    @Operation(summary = "Liberar una reserva existente por su ID")
    public ResponseEntity<ReservaResponseDTO> liberarReserva(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.liberarReserva(id));
    }

    @PutMapping("/pedidos/{pedidoId}/liberar")
    @Operation(summary = "Liberar todas las reservas asociadas a un pedido")
    public ResponseEntity<List<ReservaResponseDTO>> liberarReservasPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(servicio.liberarReservasPorPedido(pedidoId));
    }
}
