package com.fube.pedidos.clientes.dto;

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
public class ReservaStockRequestDTO {

    private Long pedidoId;

    @Builder.Default
    private List<ItemStockDTO> items = new ArrayList<>();
}
