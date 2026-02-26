package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')") // ¡Ahora esto te dejaría entrar!
    public String soloParaUsuarios() {
        return "Si estás viendo esto, es porque tu Action de Auth0 y Spring funcionan unidos.";
    }
}
