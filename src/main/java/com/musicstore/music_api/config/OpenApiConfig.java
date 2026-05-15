package com.musicstore.music_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Esta clase configura la documentación de la API
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Nombre del esquema de seguridad — se referencia en cada endpoint protegido
        final String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                // ── Información general de la API ───────────────────────────
                .info(new Info()
                        .title("Soundstore API")
                        .version("1.0.0")
                        .description("""
                                API REST para la tienda de música Soundstore.

                                ## Autenticación
                                La mayoría de endpoints requieren un JWT en el header:
                                ```
                                Authorization: Bearer <token>
                                ```
                                1. Registrate en `POST /api/v1/auth/register`
                                2. Hacé login en `POST /api/v1/auth/login`
                                3. Copiá el `token` de la respuesta
                                4. Hacé clic en **Authorize** (arriba a la derecha) y pegá el token

                                ## Credenciales de prueba
                                | Rol | Email | Contraseña |
                                |-----|-------|------------|
                                | ADMIN | admin@musicstore.com | password |
                                | CUSTOMER | juan@email.com | password |
                                """)
                        .contact(new Contact()
                                .name("Soundstore Dev Team")
                                .email("admin@musicstore.com"))
                        .license(new License()
                                .name("MIT License")))

                // ── Servidores ───────────────────────────────────────────────
                .servers(List.of(
                        new Server()
                                .url("https://sports-api-back-zd0c.onrender.com")
                                .description("Producción (Render)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Desarrollo local")
                ))

                // ── Esquema de seguridad JWT ─────────────────────────────────
                // Agrega el botón "Authorize" en Swagger UI para ingresar el token
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresá el token JWT obtenido en /auth/login (sin el prefijo 'Bearer')")))

                // Aplica el esquema JWT a TODOS los endpoints por defecto.
                // Los endpoints públicos lo ignoran porque no tienen el filtro de seguridad.
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName));
    }
}
