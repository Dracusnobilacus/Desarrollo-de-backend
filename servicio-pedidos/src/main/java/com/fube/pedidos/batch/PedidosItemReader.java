package com.fube.pedidos.batch;

import com.fube.pedidos.modelos.Pedido;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Component
public class PedidosItemReader extends JpaPagingItemReader<Pedido> {

    public PedidosItemReader(EntityManagerFactory entityManagerFactory) {
        setName("pedidosItemReader");
        setEntityManagerFactory(entityManagerFactory);
        setQueryString("SELECT p FROM Pedido p WHERE p.estado = com.fube.pedidos.modelos.EstadoPedido.CREADO AND p.fechaCreacion < :fechaLimite ORDER BY p.id ASC");
        setParameterValues(Collections.singletonMap("fechaLimite", LocalDateTime.now().minusHours(24)));
        setPageSize(10);
    }

    @Override
    protected void doOpen() throws Exception {
        setParameterValues(Collections.singletonMap("fechaLimite", LocalDateTime.now().minusHours(24)));
        super.doOpen();
    }

    @Override
    public int getPage() {
        // Como cada chunk cambia el estado de CREADO a EXPIRADO, siempre leemos la página 0 para no saltear registros
        return 0;
    }

    @Override
    public Pedido read() throws Exception {
        Pedido pedido = super.read();
        if (pedido != null) {
            log.info("Leyendo pedido id={} | estado={} | fechaCreacion={}",
                    pedido.getId(), pedido.getEstado(), pedido.getFechaCreacion());
        } else {
            log.info("Reader finalizado, no hay más pedidos para procesar");
        }
        return pedido;
    }
}
