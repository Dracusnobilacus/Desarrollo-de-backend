package com.fube.pedidos.batch;

import com.fube.pedidos.clientes.InventarioClient;
import com.fube.pedidos.clientes.dto.ItemStockDTO;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

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
            if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
                try {
                    List<ItemStockDTO> itemsStock = pedido.getItems().stream()
                            .map(item -> ItemStockDTO.builder()
                                    .productoId(item.getProductoId())
                                    .cantidad(item.getCantidad())
                                    .build())
                            .toList();

                    log.info("Llamando a servicio-inventario para liberar stock del pedido id={}", pedido.getId());
                    inventarioClient.liberarStock(pedido.getId(), itemsStock);
                    log.info("Stock liberado exitosamente para pedido id={}", pedido.getId());
                } catch (Exception e) {
                    log.error("Error al comunicarse con servicio-inventario para liberar stock del pedido id={}: {}",
                            pedido.getId(), e.getMessage());
                }
            }
        }
    }
}
