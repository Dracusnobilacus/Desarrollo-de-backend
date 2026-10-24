package com.fube.inventario.servicios;

import com.fube.inventario.dto.ProductoDTO;
import com.fube.inventario.dto.ReservaRequestDTO;
import com.fube.inventario.dto.ReservaResponseDTO;
import com.fube.inventario.excepciones.RecursoNoEncontradoException;
import com.fube.inventario.excepciones.StockInsuficienteException;
import com.fube.inventario.modelos.EstadoReserva;
import com.fube.inventario.modelos.Producto;
import com.fube.inventario.modelos.Reserva;
import com.fube.inventario.repositorio.ProductoRepositorio;
import com.fube.inventario.repositorio.ReservaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServicioTest {

    @Mock
    private ProductoRepositorio productoRepositorio;

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @InjectMocks
    private InventarioServicio inventarioServicio;

    private Producto productoPrueba;

    @BeforeEach
    void setUp() {
        productoPrueba = new Producto("Notebook Lenovo", 10);
        productoPrueba.setId(1L);
    }

    @Test
    @DisplayName("Debe crear una reserva exitosamente y descontar stock disponible")
    void crearReserva_conStockSuficiente_descuentaStockYGuardaReserva() {
        ReservaRequestDTO request = new ReservaRequestDTO(1L, 100L, 3);

        when(productoRepositorio.findById(1L)).thenReturn(Optional.of(productoPrueba));
        when(reservaRepositorio.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReservaResponseDTO respuesta = inventarioServicio.crearReserva(request);

        assertNotNull(respuesta);
        assertEquals(EstadoReserva.ACTIVA, respuesta.getEstado());
        assertEquals(3, respuesta.getCantidad());
        assertEquals(7, productoPrueba.getCantidadDisponible());

        verify(productoRepositorio).save(productoPrueba);
        verify(reservaRepositorio).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Debe lanzar StockInsuficienteException cuando la cantidad solicitada supera el stock")
    void crearReserva_conStockInsuficiente_lanzaExcepcion() {
        ReservaRequestDTO request = new ReservaRequestDTO(1L, 100L, 15);

        when(productoRepositorio.findById(1L)).thenReturn(Optional.of(productoPrueba));

        assertThrows(StockInsuficienteException.class, () -> inventarioServicio.crearReserva(request));

        assertEquals(10, productoPrueba.getCantidadDisponible());
        verify(productoRepositorio, never()).save(any());
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException cuando el producto no existe al reservar")
    void crearReserva_productoInexistente_lanzaExcepcion() {
        ReservaRequestDTO request = new ReservaRequestDTO(999L, 100L, 2);

        when(productoRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> inventarioServicio.crearReserva(request));

        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("Debe consultar la disponibilidad correctamente si el producto existe")
    void consultarDisponibilidad_productoExiste_retornaDTO() {
        when(productoRepositorio.findById(1L)).thenReturn(Optional.of(productoPrueba));

        ProductoDTO dto = inventarioServicio.consultarDisponibilidad(1L);

        assertNotNull(dto);
        assertEquals("Notebook Lenovo", dto.getNombre());
        assertEquals(10, dto.getCantidadDisponible());
    }
}
