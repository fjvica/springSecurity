package com.example.demo.config;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.io.Serializable;

/**
 * CUSTOM PERMISSION EVALUATOR (Módulo 4)
 * Se activa cuando usamos hasPermission(...) en un @PreAuthorize.
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    /**
     * @param auth Datos del usuario logueado (JWT)
     * @param targetId El ID del recurso que se quiere acceder
     * @param targetType El tipo de recurso (ej. 'MENSAJE', 'FACTURA')
     * @param permission La acción (ej. 'READ', 'WRITE')
     */
    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null || targetType == null || !(permission instanceof String)) return false;

        String user = auth.getName();
        String perm = permission.toString().toUpperCase();

        // Ejemplo: Lógica multi-tenant o basada en ID
        // Solo permitimos el acceso si el mensaje es el 100 y el usuario es 'fran'
        if ("MENSAJE".equals(targetType) && "READ".equals(perm)) {
            return targetId.equals(100) && user.contains("fran");
        }

        // Para cualquier otro caso, podrías denegar (false) o permitir por defecto (true)
        return true;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        // Se usa cuando pasas el objeto completo en lugar del ID
        return false;
    }
}