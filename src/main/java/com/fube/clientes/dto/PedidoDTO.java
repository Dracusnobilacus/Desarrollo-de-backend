package com.fube.clientes.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoDTO {

    private Long id;
    private Long clienteId;
    @Schema(example = "CREADO", allowableValues = {"CREADO", "CONFIRMADO", "CANCELADO", "EXPIRADO"})
    private String estado;
    @JsonAlias("total")
    private BigDecimal montoTotal;
    @JsonAlias("fecha")
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    @Builder.Default
    private List<ItemPedidoDTO> items = new ArrayList<>();
}
