package com.fube.inventario.mapper;

import com.fube.inventario.dto.ReservaResponseDTO;
import com.fube.inventario.modelos.Reserva;

public class ReservaMapper {

    public static ReservaResponseDTO toDTO(Reserva reserva) {
        if (reserva == null) return null;
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getProducto() != null ? reserva.getProducto().getId() : null,
                reserva.getProducto() != null ? reserva.getProducto().getNombre() : null,
                reserva.getPedidoId(),
                reserva.getCantidad(),
                reserva.getEstado()
        );
    }
}
