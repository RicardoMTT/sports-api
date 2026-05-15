package com.musicstore.music_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Health", description = "Verificación del estado de la API y sus dependencias. Endpoints públicos — no requieren autenticación.")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    // Momento en que arrancó la aplicación (se fija al instanciar el bean)
    private final Instant startTime = Instant.now();

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Ping básico — responde inmediatamente sin tocar la base de datos.
     * Ideal para balanceadores de carga y monitoreos de alta frecuencia.
     */
    @Operation(
        summary = "Ping",
        description = "Verifica que la API está levantada y respondiendo. No consulta la base de datos. Responde en < 5 ms."
    )
    @ApiResponse(responseCode = "200", description = "API operativa")
    @SecurityRequirements   // público
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    /**
     * Health detallado — verifica la conectividad con la base de datos.
     * Retorna 200 si todo está bien, 503 si la BD no responde.
     */
    @Operation(
        summary = "Health detallado",
        description = "Verifica el estado de la API y de la conexión a la base de datos. Retorna `503 Service Unavailable` si la BD no responde."
    )
    @ApiResponse(responseCode = "200", description = "API y base de datos operativas")
    @ApiResponse(responseCode = "503", description = "La base de datos no responde")
    @SecurityRequirements   // público
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("version", appVersion);
        body.put("timestamp", Instant.now().toString());
        body.put("uptime", buildUptime());

        // Verificar conexión con la BD
        Map<String, Object> db = checkDatabase();
        body.put("database", db);

        // Si la BD está caída → 503
        if ("DOWN".equals(db.get("status"))) {
            body.put("status", "DEGRADED");
            return ResponseEntity.status(503).body(body);
        }

        return ResponseEntity.ok(body);
    }

    // ─── helpers privados ────────────────────────────────────────────────────

    private Map<String, Object> checkDatabase() {
        Map<String, Object> db = new LinkedHashMap<>();
        try {
            // Consulta mínima que SQL Server responde en < 1 ms
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            db.put("status", "UP");
            db.put("type", "SQL Server");
        } catch (Exception ex) {
            db.put("status", "DOWN");
            db.put("error", ex.getMessage());
        }
        return db;
    }

    private String buildUptime() {
        long seconds = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        long hours   = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs    = seconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, secs);
    }
}
