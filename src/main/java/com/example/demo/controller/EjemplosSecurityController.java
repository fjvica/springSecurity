package com.example.demo.controller;

import com.example.demo.dto.Documento;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ejemplos")
public class EjemplosSecurityController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * 1. SEGURIDAD BÁSICA POR ROL
     * El prefijo 'ROLE_' se añade automáticamente tras el mapeo que hicimos.
     */
    @GetMapping("/solo-user")
    @PreAuthorize("hasRole('USER')")
    public String zonaUser() {
        return "Si ves esto, tienes el rol USER asignado por la Action de Auth0.";
    }

    /**
     * 2. SEGURIDAD DINÁMICA (SpEL)
     * Compara el 'sub' del token con un ID de la base de datos local.
     * Útil para: "Solo yo puedo borrar mi cuenta".
     */
    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@usuarioRepository.findById(#id).orElse(new com.example.demo.entity.Usuario()).auth0Id == authentication.name")
    public String eliminarUsuario(@PathVariable Long id) {
        return "Usuario " + id + " eliminado con éxito (o eso simulamos).";
    }

    /**
     * 3. POST-AUTHORIZE (Seguridad basada en el resultado)
     * El metodo se ejecuta, pero Spring mira el objeto retornado (returnObject).
     * Si el 'owner' no coincide con tu 'sub' de Auth0, te lanza un 403 al final.
     */
    @GetMapping("/documentos/{id}")
    @PostAuthorize("returnObject.owner == authentication.name or hasRole('ADMIN')")
    public Documento getDocumento(@PathVariable int id) {
        // Simulamos que el documento 1 es tuyo y el 2 es de otro
        // authentication.name devuelve el "sub" (google-oauth2|xxx)
        if (id == 1) {
            return new Documento(1, "Plan de dominación mundial", "google-oauth2|106562447922089855014");
        } else {
            return new Documento(id, "Lista de la compra de otro", "auth0|otro-usuario-id");
        }
    }

    /**
     * 4. MÚLTIPLES ROLES
     * Comprueba si tienes al menos uno de los dos.
     */
    @GetMapping("/premium")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String zonaPremium() {
        return "Bienvenido a la zona compartida.";
    }

    /**
     * 5. INSPECCIÓN COMPLETA (Para debug)
     * Muestra todo lo que Spring sabe de ti en este momento.
     */
    @GetMapping("/debug")
    public Map<String, Object> debug(Authentication auth) {
        return Map.of(
                "nombre_en_auth0", auth.getName(),
                "autoridades_spring", auth.getAuthorities(),
                "detalles_token", auth.getPrincipal()
        );
    }
}
