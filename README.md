# Flujo de Autenticación y Persistencia: Auth0 + Spring Boot + H2

Este documento describe el proceso técnico completo, desde que un usuario pulsa el botón de Login hasta que sus datos quedan registrados en la base de datos local.

## 1. Fase de Autenticación (Auth0)
1. **Petición de Login**: El usuario intenta acceder a una ruta protegida en Spring Boot (ej. `/api/perfil`).
2. **Redirección**: Spring Boot detecta que no hay sesión y redirige al usuario al Universal Login de Auth0.
3. **Validación**: El usuario se identifica (en este caso, mediante Google). Auth0 valida las credenciales.

## 2. Fase de Enriquecimiento (Auth0 Actions)
Antes de devolver el control a nuestra aplicación, Auth0 ejecuta una **Action (Post-Login)**:
1. **Identificación**: La Action detecta si es el primer login del usuario (`event.stats.logins_count === 1`).
2. **Llamada a Management API**:
    - La Action solicita un Token de Acceso interno.
    - Utiliza ese token para asignar automáticamente el Rol `USER` (ID: `rol_MO0QR1aCwUUJaONl`) al usuario en la base de datos de Auth0.
3. **Inyección de Claims**: La Action añade una "etiqueta personalizada" (Custom Claim) al token JWT con la URL `https://tu-app.com/roles` conteniendo el array de roles.

## 3. Fase de Recepción (Spring Boot)
Auth0 devuelve al usuario a la aplicación con un **ID Token** (un JWT). Aquí entra en juego la clase `SecurityConfig`:
1. **Intercepción**: El `filterChain` recibe el token.
2. **Mapeo de Roles (Traducción)**:
    - El `OidcUserService` personalizado extrae el Claim `https://tu-app.com/roles`.
    - Convierte el texto `USER` en una autoridad reconocida por Spring: `ROLE_USER`.
    - Esto permite el uso de la anotación `@PreAuthorize("hasRole('USER')")`.

## 4. Fase de Persistencia (JPA + H2)
Dentro del mismo `OidcUserService`, ocurre la sincronización con la base de datos local:
1. **Extracción de Identidad**: Se obtiene el `sub` (identificador único de Auth0, ej: `google-oauth2|123...`).
2. **Búsqueda en BD**: Se consulta en la tabla `usuarios` si ya existe un registro con ese `auth0Id`.
3. **Sincronización**:
    - **Si no existe**: Se crea una nueva entidad `Usuario` con el nombre, email y foto provenientes de Auth0 y se guarda en H2.
    - **Si existe**: El proceso continúa (podría usarse para actualizar datos si fuera necesario).

## 5. Acceso Concedido
1. **Creación de Sesión**: Spring Boot crea la sesión del usuario y le otorga una cookie `JSESSIONID`.
2. **Navegación**: El usuario es redirigido finalmente a la ruta solicitada inicialmente.
3. **Dashboard de H2**: Los datos ahora son visibles realizando una consulta `SELECT * FROM USUARIOS;` en la consola de H2 (`/h2-console`).

---

## Tecnologías Utilizadas
- **Auth0**: Proveedor de Identidad (IdP) y gestión de Roles.
- **Spring Security**: Gestión de filtros, OAuth2 y protección CSRF/Frames.
- **JPA / Hibernate**: Mapeo de objetos Java a tablas de base de datos.
- **H2 Database**: Base de datos en memoria para persistencia rápida en desarrollo.