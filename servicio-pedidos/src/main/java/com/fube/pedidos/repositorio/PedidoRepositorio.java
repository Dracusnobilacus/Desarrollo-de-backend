package com.fube.pedidos.repositorio;

import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByEstadoAndFechaCreacionBefore(EstadoPedido estado, LocalDateTime fecha);

    List<Pedido> findByClienteIdAndEstado(Long clienteId, EstadoPedido estado);
}
