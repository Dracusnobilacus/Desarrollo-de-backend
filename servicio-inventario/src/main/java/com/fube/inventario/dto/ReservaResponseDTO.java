package com.fube.inventario.dto;

import com.fube.inventario.modelos.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Long pedidoId;
    private Integer cantidad;
    private EstadoReserva estado;
}
