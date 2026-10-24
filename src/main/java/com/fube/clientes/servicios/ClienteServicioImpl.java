package com.fube.clientes.servicios;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.dto.PedidoDTO;
import com.fube.clientes.mapper.ClienteMapper;
import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteServicioImpl implements ClienteServicio {

    private final ClienteRepositorio repositorio;
    private final StatsigService statsigService;
    private final PedidoClient pedidoClient;

    public ClienteServicioImpl(ClienteRepositorio repositorio, StatsigService statsigService, PedidoClient pedidoClient) {
        this.repositorio = repositorio;
        this.statsigService = statsigService;
        this.pedidoClient = pedidoClient;
    }

    @Override
    public ClienteDTO crear(Cliente cliente) {
        return ClienteMapper.toDTO(repositorio.save(cliente));
    }

    @Override
    public ClienteDTO buscarPorId(Long id) {
        Cliente cliente = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        ClienteDTO dto = ClienteMapper.toDTO(cliente);

        if (statsigService.isMaskPiiEnabled(String.valueOf(id))) {
            dto.setNombre(enmascararNombre(dto.getNombre()));
            if (dto.getApellido() != null) {
                dto.setApellido(enmascararNombre(dto.getApellido()));
            }
            dto.setEmail(enmascararEmail(dto.getEmail()));
        }

        return dto;
    }

    private String enmascararNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return nombre;
        }
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            String parte = partes[i];
            if (!parte.isEmpty()) {
                sb.append(Character.toUpperCase(parte.charAt(0))).append("***");
                if (i < partes.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }

    private String enmascararEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] partes = email.split("@", 2);
        String usuario = partes[0];
        String dominio = partes[1];
        int puntoIndex = dominio.lastIndexOf('.');
        if (puntoIndex > 0) {
            String nombreDominio = dominio.substring(0, puntoIndex);
            String extension = dominio.substring(puntoIndex);
            return "*".repeat(Math.max(5, usuario.length())) + "@" + "*".repeat(Math.max(4, nombreDominio.length())) + extension;
        }
        return "*".repeat(Math.max(5, usuario.length())) + "@" + "*".repeat(Math.max(4, dominio.length()));
    }


    @Override
    public List<ClienteDTO> buscarTodos() {
        return repositorio.findAll().stream()
                .map(ClienteMapper::toDTO)
                .toList();
    }

    @Override
    public ClienteDTO actualizar(Cliente cliente) {
        return ClienteMapper.toDTO(repositorio.save(cliente));
    }

    @Override
    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    public List<PedidoDTO> buscarPedidosPorCliente(Long clienteId) {
        if (!repositorio.existsById(clienteId)) {
            throw new RuntimeException("Cliente no encontrado con id: " + clienteId);
        }
        return pedidoClient.obtenerPedidosPorCliente(clienteId);
    }
}

