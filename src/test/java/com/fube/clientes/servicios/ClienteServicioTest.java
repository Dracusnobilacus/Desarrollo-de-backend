package com.fube.clientes.servicios;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServicioTest {

    @Mock
    private ClienteRepositorio repositorio;

    @Mock
    private StatsigService statsigService;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private ClienteServicioImpl servicio;

    @Test
    @DisplayName("buscarPorId debe enmascarar nombre y email cuando el flag mask-pii es true")
    void buscarPorId_cuandoMaskPiiEsTrue_enmascaraNombreYEmail() {
        // GIVEN
        Long clienteId = 1L;
        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@example.com")
                .telefono("+54 11 1234-5678")
                .direccion("Av. Corrientes 1234")
                .build();

        when(repositorio.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(statsigService.isMaskPiiEnabled(String.valueOf(clienteId))).thenReturn(true);

        // WHEN
        ClienteDTO resultado = servicio.buscarPorId(clienteId);

        // THEN
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        assertEquals("J***", resultado.getNombre());
        assertEquals("P***", resultado.getApellido());
        assertNotEquals("juan.perez@example.com", resultado.getEmail());
        assertTrue(resultado.getEmail().contains("@"));
        assertTrue(resultado.getEmail().startsWith("*****"));

        verify(repositorio).findById(clienteId);
        verify(statsigService).isMaskPiiEnabled(String.valueOf(clienteId));
    }

    @Test
    @DisplayName("buscarPorId debe devolver datos limpios cuando el flag mask-pii es false")
    void buscarPorId_cuandoMaskPiiEsFalse_devuelveDatosLimpios() {
        // GIVEN
        Long clienteId = 1L;
        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@example.com")
                .telefono("+54 11 1234-5678")
                .direccion("Av. Corrientes 1234")
                .build();

        when(repositorio.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(statsigService.isMaskPiiEnabled(String.valueOf(clienteId))).thenReturn(false);

        // WHEN
        ClienteDTO resultado = servicio.buscarPorId(clienteId);

        // THEN
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("Pérez", resultado.getApellido());
        assertEquals("juan.perez@example.com", resultado.getEmail());
        assertEquals("+54 11 1234-5678", resultado.getTelefono());
        assertEquals("Av. Corrientes 1234", resultado.getDireccion());

        verify(repositorio).findById(clienteId);
        verify(statsigService).isMaskPiiEnabled(String.valueOf(clienteId));
    }

    @Test
    @DisplayName("buscarPorId debe lanzar RuntimeException cuando el cliente no existe")
    void buscarPorId_cuandoClienteNoExiste_lanzaExcepcion() {
        // GIVEN
        Long clienteId = 999L;
        when(repositorio.findById(clienteId)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> servicio.buscarPorId(clienteId));
        assertTrue(excepcion.getMessage().contains("Cliente no encontrado con id: " + clienteId));

        verify(repositorio).findById(clienteId);
        verifyNoInteractions(statsigService);
    }
}
