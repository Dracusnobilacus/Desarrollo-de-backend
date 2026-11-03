package com.fube.pedidos.batch;

import com.fube.pedidos.clientes.InventarioClient;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PedidosItemWriter implements ItemWriter<Pedido> {

    private final PedidoRepositorio repositorio;
    private final InventarioClient inventarioClient;

    public PedidosItemWriter(PedidoRepositorio repositorio, InventarioClient inventarioClient) {
        this.repositorio = repositorio;
        this.inventarioClient = inventarioClient;
    }

    @Override
    public void write(Chunk<? extends Pedido> chunk) throws Exception {
        log.info("Escribiendo chunk de {} pedido(s) expirados", chunk.size());
        chunk.getItems().forEach(p ->
                log.info("  -> Guardando pedido expirado id={} | clienteId={} | estado={}",
                        p.getId(), p.getClienteId(), p.getEstado())
        );

        // Guardar pedidos actualizados en base de datos
        repositorio.saveAll(chunk.getItems());
        log.info("Chunk de pedidos guardado exitosamente en BD");

        // Liberar reserva de stock correspondiente en servicio-inventario
        for (Pedido pedido : chunk.getItems()) {
            try {
                log.info("Llamando a servicio-inventario para liberar stock del pedido id={}", pedido.getId());
                boolean liberado = inventarioClient.liberarReservasPorPedido(pedido.getId());
                if (liberado) {
                    log.info("Stock liberado exitosamente en inventario para pedido id={}", pedido.getId());
                } else {
                    log.warn("No se pudo liberar stock en inventario para pedido id={}", pedido.getId());
                }
            } catch (Exception e) {
                log.error("Error al comunicarse con servicio-inventario para liberar stock del pedido id={}: {}",
                        pedido.getId(), e.getMessage());
            }
        }
    }
}
