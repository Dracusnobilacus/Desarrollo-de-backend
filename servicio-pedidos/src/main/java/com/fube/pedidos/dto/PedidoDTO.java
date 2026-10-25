package com.fube.pedidos.dto;

import com.fube.pedidos.modelos.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {

    private Long id;
    private Long clienteId;
    private LocalDateTime fechaCreacion;
    private EstadoPedido estado;
    private BigDecimal total;
    private String descripcion;
}
