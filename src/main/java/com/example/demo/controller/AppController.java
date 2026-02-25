package com.example.demo.controller;

import com.example.demo.dto.Documento;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {

    /**
     * @PreAuthorize + SpEL dinámico
     * Compara el nombre del usuario en el token con el parámetro de la URL.
     * Ideal para perfiles de usuario: "Solo yo puedo ver mi perfil o un Admin".
     */
    @GetMapping("/usuarios/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public String verPerfil(@PathVariable String username) {
        return "Perfil de: " + username;
    }

    /**
     * @PostAuthorize + returnObject
     * Se ejecuta el método completo, pero se bloquea la respuesta al final si no eres el dueño.
     * Útil cuando el 'owner' solo se conoce después de consultar la base de datos.
     */
    @GetMapping("/documentos/{id}")
    @PostAuthorize("returnObject.owner == authentication.name or hasRole('ADMIN')")
    public Documento getDocumento(@PathVariable int id) {
        // Simulación de DB. returnObject será este Documento.
        return (id == 1)
                ? new Documento(1, "Secreto", "auth0|fran123")
                : new Documento(id, "Público", "otro-id");
    }

    /**
     * hasPermission + PermissionEvaluator
     * Delega la lógica a la clase CustomPermissionEvaluator.
     */
    @GetMapping("/mensajes-especiales/{id}")
    @PreAuthorize("hasPermission(#id, 'MENSAJE', 'READ')")
    public String getMensajeEspecial(@PathVariable int id) {
        return "Acceso concedido por evaluador personalizado al ID: " + id;
    }

    /**
     * Jerarquía de Roles
     * Si el usuario tiene ROLE_ADMIN, podrá entrar aquí aunque pidamos ROLE_USER.
     */
    @GetMapping("/test/user-area")
    @PreAuthorize("hasRole('USER')")
    public String soloUsuarios() {
        return "Contenido para usuarios. Los Admins también entran por jerarquía.";
    }

    /**
     * Inspección del Contexto
     */
    @GetMapping("/quien-soy")
    public Map<String, Object> quienSoy(Authentication auth) {
        return Map.of(
                "subject_auth0", auth.getName(),
                "roles_procesados", auth.getAuthorities(),
                "claims_jwt", auth.getPrincipal()
        );
    }
}