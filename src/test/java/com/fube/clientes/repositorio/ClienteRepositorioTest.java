package com.fube.clientes.repositorio;

import com.fube.clientes.modelos.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ClienteRepositorioTest {

    @Autowired
    private ClienteRepositorio repositorio;

    @Test
    @DisplayName("findByTelefonoIsNullOrDireccionIsNull devuelve solo clientes con telefono o direccion nulos")
    void findByTelefonoIsNullOrDireccionIsNull_filtraCorrectamente() {
        // GIVEN
        repositorio.save(Cliente.builder()
                .nombre("Juan")
                .apellido("Perez")
                .email("juan.completo@example.com")
                .telefono("1111-2222")
                .direccion("Calle 123")
                .build());

        repositorio.save(Cliente.builder()
                .nombre("Maria")
                .apellido("Gomez")
                .email("maria.sintel@example.com")
                .telefono(null)
                .direccion("Calle 456")
                .build());

        repositorio.save(Cliente.builder()
                .nombre("Carlos")
                .apellido("Lopez")
                .email("carlos.sindir@example.com")
                .telefono("3333-4444")
                .direccion(null)
                .build());

        // WHEN
        List<Cliente> incompletos = repositorio.findByTelefonoIsNullOrDireccionIsNull();

        // THEN
        assertEquals(2, incompletos.size());
    }
}
