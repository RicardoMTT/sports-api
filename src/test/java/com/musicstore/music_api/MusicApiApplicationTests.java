package com.musicstore.music_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")   // activa application-test.properties
class MusicApiApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring arranca correctamente
    }
}

