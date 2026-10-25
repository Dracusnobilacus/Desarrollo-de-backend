package com.fube.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedidoRequestDTO {

    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
