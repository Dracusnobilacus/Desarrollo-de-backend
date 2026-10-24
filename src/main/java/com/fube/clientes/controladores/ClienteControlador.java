package com.fube.clientes.controladores;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.mapper.ClienteMapper;
import com.fube.clientes.servicios.ClienteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Endpoints para la gestión de clientes")
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un cliente", description = "Crea un nuevo cliente y retorna el cliente registrado")
    public ClienteDTO crear(@RequestBody ClienteDTO clienteDTO) {
        return servicio.crear(ClienteMapper.toEntity(clienteDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar datos de un cliente por id", description = "Retorna los datos de un cliente según su ID")
    public ClienteDTO buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Listar todos los clientes", description = "Retorna la lista de todos los clientes registrados")
    public List<ClienteDTO> buscarTodos() {
        return servicio.buscarTodos();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente por ID")
    public ClienteDTO actualizar(@PathVariable Long id, @RequestBody ClienteDTO clienteDTO) {
        clienteDTO.setId(id);
        return servicio.actualizar(ClienteMapper.toEntity(clienteDTO));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar cliente", description = "Elimina el cliente correspondiente al ID indicado")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    @GetMapping("/{id}/pedidos")
    @Operation(summary = "Consultar pedidos de un cliente", description = "Retorna los pedidos de un cliente determinado consultando de forma síncrona a servicio-pedidos")
    public List<com.fube.clientes.dto.PedidoDTO> buscarPedidosPorCliente(@PathVariable Long id) {
        return servicio.buscarPedidosPorCliente(id);
    }
}

