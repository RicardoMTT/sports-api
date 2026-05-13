package com.musicstore.music_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración de JPA Auditing separada de MusicApiApplication.
 *
 * Por qué está separada:
 * @WebMvcTest no carga clases de configuración JPA, pero si @EnableJpaAuditing
 * está en la clase principal (@SpringBootApplication), Spring Boot la encuentra
 * igual e intenta crear el jpaAuditingHandler, fallando porque no hay metamodelo JPA.
 *
 * Al separarlo aquí, @WebMvcTest lo excluye automáticamente junto con el resto
 * de beans de persistencia.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

