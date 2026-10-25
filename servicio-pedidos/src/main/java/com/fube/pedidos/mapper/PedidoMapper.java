package com.fube.pedidos.mapper;

import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.modelos.Pedido;

public class PedidoMapper {

    public static PedidoDTO toDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        return PedidoDTO.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .fechaCreacion(pedido.getFechaCreacion())
                .estado(pedido.getEstado())
                .total(pedido.getTotal())
                .descripcion(pedido.getDescripcion())
                .build();
    }

    public static Pedido toEntity(PedidoDTO dto) {
        if (dto == null) {
            return null;
        }

        return Pedido.builder()
                .id(dto.getId())
                .clienteId(dto.getClienteId())
                .fechaCreacion(dto.getFechaCreacion())
                .estado(dto.getEstado())
                .total(dto.getTotal())
                .descripcion(dto.getDescripcion())
                .build();
    }
}
