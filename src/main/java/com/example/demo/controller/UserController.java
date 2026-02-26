package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @GetMapping("/api/perfil")
    public Map<String, Object> getPerfil(@AuthenticationPrincipal OidcUser principal) {
        // Extraemos los roles de las autoridades que mapeamos en el paso anterior
        String roles = principal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.joining(", "));

        return Map.of(
                "mensaje", "¡Login exitoso!",
                "nombre", principal.getFullName(),
                "email", principal.getEmail(),
                "tus_roles_en_spring", roles,
                "claims_completos", principal.getClaims()
        );
    }
}