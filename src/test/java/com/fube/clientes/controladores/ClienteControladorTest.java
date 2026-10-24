package com.fube.clientes.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.excepciones.ClienteNoEncontradoException;
import com.fube.clientes.excepciones.GlobalExceptionHandler;
import com.fube.clientes.servicios.ClienteServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteControlador.class)
@Import(GlobalExceptionHandler.class)
class ClienteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteServicio servicio;

    @Test
    @DisplayName("POST /api/clientes con datos válidos retorna 201 CREATED")
    void crear_conDatosValidos_retornaCreated() throws Exception {
        ClienteDTO nuevoCliente = ClienteDTO.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@example.com")
                .build();

        ClienteDTO clienteGuardado = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@example.com")
                .fechaAlta(LocalDateTime.now())
                .build();

        when(servicio.crear(any())).thenReturn(clienteGuardado);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoCliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));
    }

    @Test
    @DisplayName("POST /api/clientes con campos inválidos retorna 400 BAD REQUEST y errores de validación")
    void crear_conCamposInvalidos_retornaBadRequest() throws Exception {
        ClienteDTO clienteInvalido = ClienteDTO.builder()
                .nombre("")
                .apellido("")
                .email("correo-invalido")
                .build();

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validaciones.nombre").exists())
                .andExpect(jsonPath("$.validaciones.apellido").exists())
                .andExpect(jsonPath("$.validaciones.email").exists());
    }

    @Test
    @DisplayName("GET /api/clientes/{id} cuando no existe retorna 404 NOT FOUND con ErrorRespuesta")
    void buscarPorId_cuandoNoExiste_retornaNotFound() throws Exception {
        Long idInexistente = 999L;
        when(servicio.buscarPorId(idInexistente)).thenThrow(new ClienteNoEncontradoException(idInexistente));

        mockMvc.perform(get("/api/clientes/{id}", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con id: " + idInexistente));
    }
}
