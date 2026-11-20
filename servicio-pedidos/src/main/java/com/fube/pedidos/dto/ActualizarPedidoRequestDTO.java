package com.fube.pedidos.dto;

import com.fube.pedidos.modelos.EstadoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPedidoRequestDTO {

    @Positive(message = "El ID del cliente debe ser mayor a 0")
    @Schema(example = "1")
    private Long clienteId;

    @Schema(example = "CREADO")
    private EstadoPedido estado;

    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    @Schema(example = "3000.00")
    private BigDecimal montoTotal;

    private LocalDateTime fechaCreacion;

    @Valid
    private List<ItemPedidoRequestDTO> items;
}
