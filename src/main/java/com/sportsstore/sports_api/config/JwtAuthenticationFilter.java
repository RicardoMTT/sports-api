package com.sportsstore.sports_api.config;

import com.sportsstore.sports_api.services.JwtService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Verificamos si el header de autorización existe y tiene el formato correcto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si no hay token, simplemente pasamos la petición al siguiente filtro.
            // (Si la ruta era privada, Spring Security la bloqueará más adelante; si era pública, pasará).
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraemos el token puro (saltamos la palabra "Bearer " que tiene 7 caracteres)
        jwt = authHeader.substring(7);

        // 3. Desciframos el correo desde el token
        userEmail = jwtService.extractUsername(jwt);

        // 4. Validamos que haya un correo y que el usuario NO esté ya autenticado en este contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Vamos a la base de datos a traer los detalles reales del usuario
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Validamos matemáticamente el token contra los datos del usuario
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Creamos el objeto de autenticación que Spring Security entiende
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No pasamos la contraseña aquí por motivos de seguridad
                        userDetails.getAuthorities() // Le pasamos los roles (ej. ROLE_CUSTOMER)
                );

                // Le agregamos detalles extra de la petición web (IP, sesión, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ¡Momento Clave! Actualizamos el contexto global de seguridad.
                // A partir de esta línea, Spring sabe exactamente quién está haciendo la petición.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Finalmente, dejamos que la petición continúe su camino hacia el Controlador
        filterChain.doFilter(request, response);
    }

}
