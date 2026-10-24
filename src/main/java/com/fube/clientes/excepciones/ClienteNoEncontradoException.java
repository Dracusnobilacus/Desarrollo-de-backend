package com.fube.clientes.excepciones;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(Long id) {
        super("Cliente no encontrado con id: " + id);
    }

    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
