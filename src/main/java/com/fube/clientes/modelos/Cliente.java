package com.fube.clientes.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;

    @Column
    private String telefono;

    @Column
    private String direccion;

    @PrePersist
    public void prePersist() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
}
