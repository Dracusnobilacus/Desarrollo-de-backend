package com.fube.pedidos.mapper;

import com.fube.pedidos.dto.ItemPedidoDTO;
import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.modelos.ItemPedido;
import com.fube.pedidos.modelos.Pedido;

import java.util.ArrayList;
import java.util.List;

public class PedidoMapper {

    public static PedidoDTO toDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        List<ItemPedidoDTO> itemsDTO = new ArrayList<>();
        if (pedido.getItems() != null) {
            itemsDTO = pedido.getItems().stream()
                    .map(PedidoMapper::itemToDTO)
                    .toList();
        }

        return PedidoDTO.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .estado(pedido.getEstado())
                .montoTotal(pedido.getMontoTotal())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaActualizacion(pedido.getFechaActualizacion())
                .items(itemsDTO)
                .build();
    }

    public static Pedido toEntity(PedidoDTO dto) {
        if (dto == null) {
            return null;
        }

        Pedido pedido = Pedido.builder()
                .id(dto.getId())
                .clienteId(dto.getClienteId())
                .estado(dto.getEstado())
                .montoTotal(dto.getMontoTotal())
                .fechaCreacion(dto.getFechaCreacion())
                .fechaActualizacion(dto.getFechaActualizacion())
                .build();

        if (dto.getItems() != null) {
            for (ItemPedidoDTO itemDto : dto.getItems()) {
                ItemPedido item = itemToEntity(itemDto);
                pedido.addItem(item);
            }
        }

        return pedido;
    }

    public static ItemPedidoDTO itemToDTO(ItemPedido item) {
        if (item == null) {
            return null;
        }

        return ItemPedidoDTO.builder()
                .id(item.getId())
                .productoId(item.getProductoId())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }

    public static ItemPedido itemToEntity(ItemPedidoDTO dto) {
        if (dto == null) {
            return null;
        }

        return ItemPedido.builder()
                .id(dto.getId())
                .productoId(dto.getProductoId())
                .cantidad(dto.getCantidad())
                .precioUnitario(dto.getPrecioUnitario())
                .build();
    }
}
