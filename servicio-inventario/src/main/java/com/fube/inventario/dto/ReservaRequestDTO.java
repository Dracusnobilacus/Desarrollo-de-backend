package com.fube.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequestDTO {
    private Long productoId;
    private Long pedidoId;
    private Integer cantidad;
}
