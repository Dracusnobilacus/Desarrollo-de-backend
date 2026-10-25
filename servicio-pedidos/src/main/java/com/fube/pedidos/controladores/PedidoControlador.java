package com.fube.pedidos.controladores;

import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.mapper.PedidoMapper;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.servicios.PedidoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;

    public PedidoControlador(PedidoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<com.fube.pedidos.dto.PedidoResponseDTO> crear(@RequestBody com.fube.pedidos.dto.CrearPedidoRequestDTO request) {
        com.fube.pedidos.dto.PedidoResponseDTO creado = servicio.crearPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.buscarPorId(id));
    }

    @GetMapping
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

    @PutMapping("/{id}")
    public ResponseEntity<PedidoDTO> actualizar(@PathVariable Long id, @RequestBody PedidoDTO pedidoDTO) {
        pedidoDTO.setId(id);
        return ResponseEntity.ok(servicio.actualizar(PedidoMapper.toEntity(pedidoDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
