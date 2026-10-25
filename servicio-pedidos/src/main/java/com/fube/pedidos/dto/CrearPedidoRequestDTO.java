package com.fube.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPedidoRequestDTO {

    private Long clienteId;

    @Builder.Default
    private List<ItemPedidoRequestDTO> items = new ArrayList<>();
}
