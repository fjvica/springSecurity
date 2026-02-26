package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String auth0Id; // Aquí guardaremos el "sub" (ej: google-oauth2|...)

    private String nombre;
    private String email;
    private String imagen;
}
