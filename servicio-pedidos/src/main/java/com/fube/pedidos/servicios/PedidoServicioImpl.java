package com.fube.pedidos.servicios;

import com.fube.pedidos.clientes.ClienteClient;
import com.fube.pedidos.clientes.InventarioClient;
import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.excepciones.RecursoNoEncontradoException;
import com.fube.pedidos.excepciones.StockInsuficienteException;
import com.fube.pedidos.mapper.PedidoMapper;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.ItemPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PedidoServicioImpl implements PedidoServicio {

    private final PedidoRepositorio repositorio;
    private final ClienteClient clienteClient;
    private final InventarioClient inventarioClient;

    public PedidoServicioImpl(PedidoRepositorio repositorio,
                              ClienteClient clienteClient,
                              InventarioClient inventarioClient) {
        this.repositorio = repositorio;
        this.clienteClient = clienteClient;
        this.inventarioClient = inventarioClient;
    }

    @Override
    @Transactional
    public PedidoDTO crear(Pedido pedido) {
        log.info("Iniciando creación de pedido para clienteId={}", pedido.getClienteId());

        // 1. Verificar existencia del cliente en servicio-clientes
        if (!clienteClient.existeCliente(pedido.getClienteId())) {
            log.warn("Cliente no encontrado con id={}", pedido.getClienteId());
            throw new RecursoNoEncontradoException("No existe el cliente con ID: " + pedido.getClienteId());
        }

        // 2. Validar que tenga ítems
        if (pedido.getItems() == null || pedido.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto");
        }

        // 3. Establecer estado inicial y calcular monto total
        pedido.setEstado(EstadoPedido.CREADO);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedido.calcularMontoTotal();

        // 4. Guardar preliminarmente para obtener el ID del pedido
        Pedido pedidoGuardado = repositorio.save(pedido);

        // 5. Reservar el stock de cada ítem contra servicio-inventario
        for (ItemPedido item : pedidoGuardado.getItems()) {
            boolean reservado = inventarioClient.reservarItem(
                    pedidoGuardado.getId(),
                    item.getProductoId(),
                    item.getCantidad()
            );

            if (!reservado) {
                log.warn("Stock insuficiente en servicio-inventario para productoId={}", item.getProductoId());
                // Si falla uno, liberamos las reservas que se hayan hecho previamente para este pedido
                inventarioClient.liberarReservasPorPedido(pedidoGuardado.getId());
                // Eliminamos el pedido de la base de datos para no dejarlo inconsistente
                repositorio.delete(pedidoGuardado);
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto ID " + item.getProductoId() + ". El pedido no pudo crearse."
                );
            }
        }

        log.info("Pedido id={} creado exitosamente con montoTotal={}", pedidoGuardado.getId(), pedidoGuardado.getMontoTotal());
        return PedidoMapper.toDTO(pedidoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(PedidoMapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con ID: " + id));
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con ID: " + pedido.getId()));

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
    public PedidoDTO cancelar(Long id) {
        Pedido pedido = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con ID: " + id));

        if (pedido.getEstado() != EstadoPedido.CREADO && pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar pedidos en estado CREADO o CONFIRMADO. Estado actual: " + pedido.getEstado()
            );
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaActualizacion(LocalDateTime.now());

        // Liberar reserva de stock asociada en servicio-inventario
        inventarioClient.liberarReservasPorPedido(pedido.getId());
        log.info("Pedido id={} cancelado y stock liberado en servicio-inventario", id);

        return PedidoMapper.toDTO(repositorio.save(pedido));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("Pedido no encontrado con ID: " + id);
        }
        repositorio.deleteById(id);
    }
}
