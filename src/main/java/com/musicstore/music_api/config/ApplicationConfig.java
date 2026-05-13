package com.musicstore.music_api.config;

import com.musicstore.music_api.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {


    //En Spring, la anotación @Bean dentro de una clase @Configuration significa:
    //"Ejecuta este método al arrancar la aplicación, toma el objeto que devuelve,
    //y guárdalo en tu memoria global (el Contexto de Spring) para que cualquier
    // otra clase pueda pedirlo cuando lo necesite".

    private final UserRepository userRepository;

    public ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 1. GESTOR DE USUARIOS:
     * Le decimos a Spring cómo ir a nuestra base de datos SQL Server
     * a buscar al usuario utilizando su correo electrónico.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + username));
    }

    /**
     * 2. PROVEEDOR DE AUTENTICACIÓN:
     * Es el componente que se encarga de buscar los datos del usuario (usando el UserDetailsService)
     * y de verificar la contraseña (usando el PasswordEncoder).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 3. ADMINISTRADOR DE AUTENTICACIÓN:
     * Este es el "jefe". Lo inyectaremos más adelante en nuestro controlador de Login
     * para disparar el proceso de validación con una sola línea de código.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 4. ENCRIPTADOR DE CONTRASEÑAS:
     * Define el algoritmo BCrypt. Spring lo usará automáticamente para comparar
     * la contraseña enviada en el login con el hash de la BD.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

