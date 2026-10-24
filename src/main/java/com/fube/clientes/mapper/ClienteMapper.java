package com.fube.clientes.mapper;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.modelos.Cliente;

public class ClienteMapper {

    public static ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .fechaAlta(cliente.getFechaAlta())
                .build();
    }

    public static Cliente toEntity(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        return Cliente.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .fechaAlta(dto.getFechaAlta())
                .build();
    }
}
