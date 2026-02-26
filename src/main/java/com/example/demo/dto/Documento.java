package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Documento {
    private int id;
    private String contenido;
    private String owner; // Aquí guardaremos el 'sub' de Auth0
}
