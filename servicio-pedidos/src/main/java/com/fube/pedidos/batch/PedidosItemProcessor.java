package com.fube.pedidos.batch;

import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class PedidosItemProcessor implements ItemProcessor<Pedido, Pedido> {

    @Override
    public Pedido process(Pedido pedido) {
        log.info("Procesando pedido id={} para expiración", pedido.getId());

        pedido.setEstado(EstadoPedido.EXPIRADO);
        pedido.setFechaActualizacion(LocalDateTime.now());
        log.info("  -> Pedido id={} actualizado a estado EXPIRADO", pedido.getId());

        return pedido;
    }
}
