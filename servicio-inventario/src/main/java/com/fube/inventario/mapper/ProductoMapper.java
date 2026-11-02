package com.fube.inventario.mapper;

import com.fube.inventario.dto.ProductoDTO;
import com.fube.inventario.modelos.Producto;

public class ProductoMapper {

    public static ProductoDTO toDTO(Producto producto) {
        if (producto == null) return null;
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getCantidadDisponible()
        );
    }

    public static Producto toEntity(ProductoDTO dto) {
        if (dto == null) return null;
        Producto producto = new Producto(dto.getNombre(), dto.getCantidadDisponible());
        producto.setId(dto.getId());
        return producto;
    }
}
