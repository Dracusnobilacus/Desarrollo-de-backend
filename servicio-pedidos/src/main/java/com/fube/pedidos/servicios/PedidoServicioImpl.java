package com.fube.pedidos.servicios;

import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.mapper.PedidoMapper;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.ItemPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoServicioImpl implements PedidoServicio {

    private final PedidoRepositorio repositorio;

    public PedidoServicioImpl(PedidoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public PedidoDTO crear(Pedido pedido) {
        return PedidoMapper.toDTO(repositorio.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(PedidoMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> buscarTodos() {
        return repositorio.findAll().stream()
                .map(PedidoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> buscarPorClienteId(Long clienteId) {
        return repositorio.findByClienteId(clienteId).stream()
                .map(PedidoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> buscarPorEstado(EstadoPedido estado) {
        return repositorio.findByEstado(estado).stream()
                .map(PedidoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoDTO actualizar(Pedido pedido) {
        Pedido existente = repositorio.findById(pedido.getId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + pedido.getId()));

        if (pedido.getClienteId() != null) {
            existente.setClienteId(pedido.getClienteId());
        }
        if (pedido.getEstado() != null) {
            existente.setEstado(pedido.getEstado());
        }
        if (pedido.getMontoTotal() != null) {
            existente.setMontoTotal(pedido.getMontoTotal());
        }
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            existente.getItems().clear();
            for (ItemPedido item : pedido.getItems()) {
                existente.addItem(item);
            }
            if (pedido.getMontoTotal() == null) {
                existente.calcularMontoTotal();
            }
        }

        return PedidoMapper.toDTO(repositorio.save(existente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado con id: " + id);
        }
        repositorio.deleteById(id);
    }
}
