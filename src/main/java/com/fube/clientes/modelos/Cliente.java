package com.fube.clientes.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String telefono;

    @Column
    private String direccion;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    private java.time.LocalDateTime fechaAlta;

    @PrePersist
    public void prePersist() {
        if (this.fechaAlta == null) {
            this.fechaAlta = java.time.LocalDateTime.now();
        }
    }
}
