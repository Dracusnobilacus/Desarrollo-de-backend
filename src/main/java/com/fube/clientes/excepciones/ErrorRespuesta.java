package com.fube.clientes.excepciones;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorRespuesta {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private Map<String, String> validaciones;
}
