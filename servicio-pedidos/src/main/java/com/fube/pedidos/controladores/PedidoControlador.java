package com.fube.pedidos.controladores;

import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.mapper.PedidoMapper;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.servicios.PedidoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para la gestión de pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;

    public PedidoControlador(PedidoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @Operation(summary = "Crear un pedido", description = "Verifica cliente, reserva stock de cada ítem, calcula total y registra el pedido")
    public ResponseEntity<PedidoDTO> crear(@RequestBody PedidoDTO pedidoDTO) {
        PedidoDTO creado = servicio.crear(PedidoMapper.toEntity(pedidoDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un pedido mediante su identificador")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar los pedidos de un cliente (consumido por servicio-clientes)")
    public ResponseEntity<List<PedidoDTO>> buscarPorClienteId(@PathVariable Long clienteId) {
        return ResponseEntity.ok(servicio.buscarPorClienteId(clienteId));
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos o filtrar por cliente / estado")
    public ResponseEntity<List<PedidoDTO>> buscarTodos(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstadoPedido estado) {
        if (clienteId != null) {
            return ResponseEntity.ok(servicio.buscarPorClienteId(clienteId));
        }
        if (estado != null) {
            return ResponseEntity.ok(servicio.buscarPorEstado(estado));
        }
        return ResponseEntity.ok(servicio.buscarTodos());
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar un pedido", description = "Cancela un pedido en estado CREADO o CONFIRMADO y libera la reserva de stock en inventario")
    public ResponseEntity<PedidoDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.cancelar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un pedido")
    public ResponseEntity<PedidoDTO> actualizar(@PathVariable Long id, @RequestBody PedidoDTO pedidoDTO) {
        pedidoDTO.setId(id);
        return ResponseEntity.ok(servicio.actualizar(PedidoMapper.toEntity(pedidoDTO)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pedido")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
