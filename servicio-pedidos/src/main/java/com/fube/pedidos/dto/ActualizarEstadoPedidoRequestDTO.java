package com.fube.pedidos.dto;

import com.fube.pedidos.modelos.EstadoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoPedidoRequestDTO {

    @NotNull(message = "El estado es obligatorio")
    @Schema(example = "CONFIRMADO", allowableValues = {"CONFIRMADO", "CANCELADO"})
    private EstadoPedido estado;
}
