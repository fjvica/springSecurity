package com.example.demo.config;

import com.example.demo.controller.OidcLogoutSuccessHandler;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Permitir acceso a la consola de H2 sin login
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 2. IMPORTANTE: Desactivar CSRF para H2 (si no, no podrás hacer login en la consola)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                // 3. PERMITIR FRAMES: H2 los necesita para mostrar su interfaz
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService())
                        )
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(new OidcLogoutSuccessHandler())
                );

        return http.build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);

            // 1. Extraer datos del token
            String sub = oidcUser.getSubject();
            String email = oidcUser.getEmail();
            String name = oidcUser.getFullName();
            String picture = oidcUser.getPicture();

            // 2. Guardar o actualizar en H2
            usuarioRepository.findByAuth0Id(sub).orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setAuth0Id(sub);
                nuevo.setEmail(email);
                nuevo.setNombre(name);
                nuevo.setImagen(picture);
                return usuarioRepository.save(nuevo);
            });

            // 3. Mantener la lógica de roles que ya teníamos
            List<String> roles = oidcUser.getClaimAsStringList("https://tu-app.com/roles");
            if (roles == null) roles = List.of();
            Collection<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        // En Spring Security 6+ se usa el método estático fromHierarchy
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    public DefaultHttpSecurityExpressionHandler expressionHandler() {
        DefaultHttpSecurityExpressionHandler expressionHandler = new DefaultHttpSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }
}