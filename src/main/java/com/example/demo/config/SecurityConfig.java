package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * MÓDULO 3 Y 4: Configuración de Seguridad
 * @EnableWebSecurity: Habilita la seguridad web.
 * @EnableMethodSecurity: Activa las anotaciones @PreAuthorize, @PostAuthorize y @Secured.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitado para APIs stateless (JWT)
                .authorizeHttpRequests(authorize -> authorize
                        // Nota: La seguridad granular la manejaremos en los controladores con anotaciones.
                        // Aquí solo aseguramos que cualquier petición requiera token.
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    /**
     * JERARQUÍA DE ROLES (Módulo 4)
     * Define que un ADMIN tiene automáticamente los permisos de MANAGER, y este los de USER.
     * Evita tener que asignar múltiples roles a un mismo usuario en Auth0.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("MANAGER")
                .role("MANAGER").implies("USER")
                .build();
    }

    /**
     * CONVERSOR DE JWT (Módulo 3)
     * Auth0 envía los permisos en el claim "scope" como "read:messages".
     * Este bean los transforma de "SCOPE_read:messages" a "ROLE_read:messages" para que
     * Spring pueda tratarlos como Roles tradicionales con hasRole().
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        // LE DECIMOS QUE BUSQUE AQUÍ:
        converter.setAuthoritiesClaimName("https://misitio.com/roles");
        converter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }

    /**
     * HANDLER DE EXPRESIONES (Módulo 4)
     * Necesario para registrar nuestro evaluador de permisos personalizado (PermissionEvaluator).
     * El 'static' es importante para evitar problemas de ciclo de vida en Beans de Spring.
     */
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator evaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(evaluator);
        return handler;
    }
}