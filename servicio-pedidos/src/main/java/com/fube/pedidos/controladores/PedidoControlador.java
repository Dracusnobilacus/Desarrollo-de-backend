package com.fube.pedidos.controladores;

import com.fube.pedidos.dto.ActualizarPedidoRequestDTO;
import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.mapper.PedidoMapper;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.servicios.PedidoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para la gestión de pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;
    private final JobLauncher jobLauncher;
    private final Job expirarPedidosJob;

    public PedidoControlador(PedidoServicio servicio,
            JobLauncher jobLauncher,
            Job expirarPedidosJob) {
        this.servicio = servicio;
        this.jobLauncher = jobLauncher;
        this.expirarPedidosJob = expirarPedidosJob;
    }

    @PostMapping
    @Operation(summary = "Crear un pedido", description = "Verifica cliente, reserva stock de cada ítem, calcula total y registra el pedido")
    public ResponseEntity<PedidoDTO> crear(@Valid @RequestBody CrearPedidoRequestDTO request) {
        PedidoDTO creado = servicio.crear(PedidoMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un pedido mediante su identificador")

    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(servicio.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar los pedidos de un cliente (consumido por servicio-clientes)")

    public ResponseEntity<List<PedidoDTO>> buscarPorClienteId(@PathVariable("clienteId") Long clienteId) {
        return ResponseEntity.ok(servicio.buscarPorClienteId(clienteId));
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos o filtrar por cliente / estado")
    public ResponseEntity<List<PedidoDTO>> buscarTodos(
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "estado", required = false) EstadoPedido estado) {
        if (clienteId != null) {
            return ResponseEntity.ok(servicio.buscarPorClienteId(clienteId));
        }
        if (estado != null) {
            return ResponseEntity.ok(servicio.buscarPorEstado(estado));
        }
        return ResponseEntity.ok(servicio.buscarTodos());
    }

    @PutMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar un pedido", description = "Pasa un pedido de estado CREADO a CONFIRMADO")

    public ResponseEntity<PedidoDTO> confirmar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(servicio.confirmar(id));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar un pedido", description = "Cancela un pedido en estado CREADO o CONFIRMADO y libera la reserva de stock en inventario")

    public ResponseEntity<PedidoDTO> cancelar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(servicio.cancelar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un pedido")
    public ResponseEntity<PedidoDTO> actualizar(@PathVariable("id") Long id,
                                                @Valid @RequestBody ActualizarPedidoRequestDTO request) {
        return ResponseEntity.ok(servicio.actualizar(PedidoMapper.toEntity(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pedido")

    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch/expirar")
    @Operation(summary = "Ejecutar manualmente el batch de expiración de pedidos", description = "Dispara el job de Spring Batch para expirar pedidos en CREADO con más de 24hs y liberar stock en inventario")
    public ResponseEntity<String> ejecutarBatch() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(expirarPedidosJob, params);
        return ResponseEntity.ok("Batch de expiración ejecutado exitosamente");
    }
}
