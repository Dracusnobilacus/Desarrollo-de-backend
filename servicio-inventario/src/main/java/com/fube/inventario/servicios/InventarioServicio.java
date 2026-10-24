package com.fube.inventario.servicios;

import com.fube.inventario.dto.ProductoDTO;
import com.fube.inventario.dto.ReservaRequestDTO;
import com.fube.inventario.dto.ReservaResponseDTO;
import com.fube.inventario.excepciones.RecursoNoEncontradoException;
import com.fube.inventario.excepciones.StockInsuficienteException;
import com.fube.inventario.mapper.ProductoMapper;
import com.fube.inventario.mapper.ReservaMapper;
import com.fube.inventario.modelos.EstadoReserva;
import com.fube.inventario.modelos.Producto;
import com.fube.inventario.modelos.Reserva;
import com.fube.inventario.repositorio.ProductoRepositorio;
import com.fube.inventario.repositorio.ReservaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioServicio {

    private final ProductoRepositorio productoRepositorio;
    private final ReservaRepositorio reservaRepositorio;

    public InventarioServicio(ProductoRepositorio productoRepositorio, ReservaRepositorio reservaRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
    }

    public ProductoDTO consultarDisponibilidad(Long productoId) {
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + productoId));
        return ProductoMapper.toDTO(producto);
    }

    public List<ProductoDTO> listarProductos() {
        return productoRepositorio.findAll().stream()
                .map(ProductoMapper::toDTO)
                .toList();
    }

    @Transactional
    public ReservaResponseDTO crearReserva(ReservaRequestDTO request) {
        Producto producto = productoRepositorio.findById(request.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + request.getProductoId()));

        if (producto.getCantidadDisponible() < request.getCantidad()) {
            throw new StockInsuficienteException(
                    String.format("Stock insuficiente para el producto '%s'. Disponible: %d, Solicitado: %d",
                            producto.getNombre(), producto.getCantidadDisponible(), request.getCantidad())
            );
        }

        producto.setCantidadDisponible(producto.getCantidadDisponible() - request.getCantidad());
        productoRepositorio.save(producto);

        Reserva reserva = new Reserva(producto, request.getPedidoId(), request.getCantidad(), EstadoReserva.ACTIVA);
        Reserva guardada = reservaRepositorio.save(reserva);

        return ReservaMapper.toDTO(guardada);
    }
}
