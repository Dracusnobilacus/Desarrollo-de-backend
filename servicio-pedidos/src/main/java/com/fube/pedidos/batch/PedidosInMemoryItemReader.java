package com.fube.pedidos.batch;

import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class PedidosInMemoryItemReader extends ListItemReader<Pedido> {

    public PedidosInMemoryItemReader(PedidoRepositorio repositorio) {
        super(cargarPedidos(repositorio));
    }

    private static List<Pedido> cargarPedidos(PedidoRepositorio repositorio) {
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
        List<Pedido> pedidos = repositorio.findByEstadoAndFechaCreacionBefore(EstadoPedido.CREADO, hace24Horas);
        log.info("Pedidos CREADOS con más de 24hs cargados en memoria: {}", pedidos.size());
        return pedidos;
    }
}
