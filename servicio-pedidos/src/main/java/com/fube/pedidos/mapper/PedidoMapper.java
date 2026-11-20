package com.fube.pedidos.mapper;

import com.fube.pedidos.dto.*;
import com.fube.pedidos.modelos.ItemPedido;
import com.fube.pedidos.modelos.Pedido;

import java.util.ArrayList;
import java.util.List;

public class PedidoMapper {

    // --- Mapeos para Request DTO ---

    public static Pedido toEntity(CrearPedidoRequestDTO request) {
        if (request == null) {
            return null;
        }

        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .build();

        if (request.getItems() != null) {
            for (ItemPedidoRequestDTO itemDto : request.getItems()) {
                ItemPedido item = ItemPedido.builder()
                        .productoId(itemDto.getProductoId())
                        .cantidad(itemDto.getCantidad())
                        .precioUnitario(itemDto.getPrecioUnitario())
                        .build();
                pedido.addItem(item);
            }
        }

        pedido.calcularMontoTotal();
        return pedido;
    }

    public static Pedido toEntity(Long id, ActualizarPedidoRequestDTO request) {
        if (request == null) {
            return null;
        }

        Pedido pedido = Pedido.builder()
                .id(id)
                .clienteId(request.getClienteId())
                .estado(request.getEstado())
                .montoTotal(request.getMontoTotal())
                .fechaCreacion(request.getFechaCreacion())
                .build();

        if (request.getItems() != null) {
            for (ItemPedidoRequestDTO itemDto : request.getItems()) {
                ItemPedido item = ItemPedido.builder()
                        .productoId(itemDto.getProductoId())
                        .cantidad(itemDto.getCantidad())
                        .precioUnitario(itemDto.getPrecioUnitario())
                        .build();
                pedido.addItem(item);
            }
        }

        return pedido;
    }

    // --- Mapeos para PedidoDTO ---

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
