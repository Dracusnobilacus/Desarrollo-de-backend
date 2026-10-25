package com.fube.pedidos.servicios;

import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.dto.PedidoResponseDTO;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;

import java.util.List;

public interface PedidoServicio {

    PedidoResponseDTO crearPedido(CrearPedidoRequestDTO request);

    PedidoResponseDTO crearPedido(Pedido pedido);

    PedidoResponseDTO cancelarPedido(Long id);

    PedidoDTO crear(Pedido pedido);

    PedidoDTO buscarPorId(Long id);

    List<PedidoDTO> buscarTodos();

    List<PedidoDTO> buscarPorClienteId(Long clienteId);

    List<PedidoDTO> buscarPorEstado(EstadoPedido estado);

    PedidoDTO actualizar(Pedido pedido);

    void eliminar(Long id);
}
