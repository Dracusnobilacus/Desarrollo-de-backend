package com.fube.inventario.repositorio;

import com.fube.inventario.modelos.EstadoReserva;
import com.fube.inventario.modelos.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    List<Reserva> findByPedidoId(Long pedidoId);

    List<Reserva> findByPedidoIdAndEstado(Long pedidoId, EstadoReserva estado);

    List<Reserva> findByProductoIdAndEstado(Long productoId, EstadoReserva estado);
}
