package com.fube.pedidos.servicios;

import com.fube.pedidos.clientes.ClienteClient;
import com.fube.pedidos.clientes.InventarioClient;
import com.fube.pedidos.clientes.dto.ItemStockDTO;
import com.fube.pedidos.dto.CrearPedidoRequestDTO;
import com.fube.pedidos.dto.ItemPedidoRequestDTO;
import com.fube.pedidos.dto.PedidoDTO;
import com.fube.pedidos.dto.PedidoResponseDTO;
import com.fube.pedidos.mapper.PedidoMapper;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.ItemPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Crea un pedido ejecutando el flujo completo:
     * 1) Valida existencia del cliente con ClienteClient.
     * 2) Para cada ítem, reserva el stock necesario con InventarioClient.
     * 3) Calcula el monto total del pedido.
     * 4) Guarda el pedido con estado CREADO.
     * Si algo falla en cualquier paso, se aborta y se libera el stock reservado.
     */
    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(CrearPedidoRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de creación de pedido no puede ser nula.");
        }
        if (request.getClienteId() == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un ítem.");
        }

        // 1) Validar cliente con ClienteClient (puerto 8080)
        boolean existeCliente = clienteClient.existeCliente(request.getClienteId());
        if (!existeCliente) {
            throw new RuntimeException("El cliente con ID " + request.getClienteId() + " no existe.");
        }

        // 2) Para cada ítem llamar a reservar stock con InventarioClient (puerto 8081)
        List<ItemStockDTO> itemsReservados = new ArrayList<>();
        try {
            for (ItemPedidoRequestDTO itemDto : request.getItems()) {
                if (itemDto.getProductoId() == null) {
                    throw new IllegalArgumentException("El ID de producto es obligatorio en cada ítem.");
                }
                if (itemDto.getCantidad() == null || itemDto.getCantidad() <= 0) {
                    throw new IllegalArgumentException("La cantidad para el producto " + itemDto.getProductoId() + " debe ser mayor a cero.");
                }

                boolean reservado = inventarioClient.reservarStockItem(itemDto.getProductoId(), itemDto.getCantidad());
                if (!reservado) {
                    throw new RuntimeException("Stock insuficiente para el producto ID " + itemDto.getProductoId());
                }

                itemsReservados.add(new ItemStockDTO(itemDto.getProductoId(), itemDto.getCantidad()));
            }

            // 3) Calcular montoTotal y armar la entidad
            Pedido pedido = Pedido.builder()
                    .clienteId(request.getClienteId())
                    .estado(EstadoPedido.CREADO)
                    .build();

            for (ItemPedidoRequestDTO itemDto : request.getItems()) {
                ItemPedido itemPedido = ItemPedido.builder()
                        .productoId(itemDto.getProductoId())
                        .cantidad(itemDto.getCantidad())
                        .precioUnitario(itemDto.getPrecioUnitario())
                        .build();
                pedido.addItem(itemPedido);
            }

            pedido.calcularMontoTotal();

            // 4) Guardar pedido como CREADO
            Pedido guardado = repositorio.save(pedido);
            return PedidoMapper.toResponseDTO(guardado);

        } catch (Exception e) {
            // Si algo falla, abortar y liberar el stock reservado hasta el momento
            for (ItemStockDTO reservado : itemsReservados) {
                try {
                    inventarioClient.liberarStockItem(reservado.getProductoId(), reservado.getCantidad());
                } catch (Exception ex) {
                    // Evitar enmascarar la excepción original
                }
            }
            throw new RuntimeException("Error al crear pedido: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser nulo.");
        }
        CrearPedidoRequestDTO request = CrearPedidoRequestDTO.builder()
                .clienteId(pedido.getClienteId())
                .items(pedido.getItems() != null ? pedido.getItems().stream()
                        .map(i -> ItemPedidoRequestDTO.builder()
                                .productoId(i.getProductoId())
                                .cantidad(i.getCantidad())
                                .precioUnitario(i.getPrecioUnitario())
                                .build())
                        .toList() : new ArrayList<>())
                .build();
        return crearPedido(request);
    }

    @Override
    @Transactional
    public PedidoResponseDTO cancelarPedido(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del pedido no puede ser nulo.");
        }

        Pedido pedido = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        // Validar que el pedido esté CREADO o CONFIRMADO
        if (pedido.getEstado() != EstadoPedido.CREADO && pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new IllegalStateException("No se puede cancelar el pedido con id " + id +
                    " porque se encuentra en estado " + pedido.getEstado() +
                    ". Solo se pueden cancelar pedidos en estado CREADO o CONFIRMADO.");
        }

        // Cambiar estado a CANCELADO
        pedido.setEstado(EstadoPedido.CANCELADO);

        // Llamar a InventarioClient.liberarReserva
        List<ItemStockDTO> itemsParaLiberar = (pedido.getItems() != null) ? pedido.getItems().stream()
                .map(item -> new ItemStockDTO(item.getProductoId(), item.getCantidad()))
                .toList() : java.util.Collections.emptyList();

        inventarioClient.liberarReserva(pedido.getId(), itemsParaLiberar);

        // Guardar pedido actualizado
        Pedido guardado = repositorio.save(pedido);
        return PedidoMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public PedidoDTO crear(Pedido pedido) {
        PedidoResponseDTO response = crearPedido(pedido);
        return PedidoDTO.builder()
                .id(response.getId())
                .clienteId(response.getClienteId())
                .estado(response.getEstado())
                .montoTotal(response.getMontoTotal())
                .fechaCreacion(response.getFechaCreacion())
                .fechaActualizacion(response.getFechaActualizacion())
                .build();
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
