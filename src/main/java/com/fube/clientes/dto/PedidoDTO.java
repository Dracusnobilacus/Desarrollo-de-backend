package com.fube.clientes.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoDTO {

    private Long id;
    private Long clienteId;
    private String descripcion;
    @JsonAlias("montoTotal")
    private BigDecimal total;
    private String estado;
    @JsonAlias("fechaCreacion")
    private LocalDateTime fecha;
}
