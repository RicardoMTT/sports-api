package com.musicstore.music_api.controller;

import com.musicstore.music_api.domain.dtos.AuthenticationRequest;
import com.musicstore.music_api.domain.dtos.AuthenticationResponse;
import com.musicstore.music_api.domain.dtos.RefreshRequest;
import com.musicstore.music_api.domain.dtos.RegisterRequest;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Registro, login, refresh y logout de usuarios")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de tipo CUSTOMER y devuelve el JWT de acceso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o email ya registrado")
    })
    @SecurityRequirements  // endpoint público — no requiere JWT
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve el access token (15 min) y refresh token (7 días)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    })
    @SecurityRequirements  // endpoint público — no requiere JWT
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @Operation(summary = "Renovar access token", description = "Genera un nuevo access token usando el refresh token. Usar cuando el access token expira (401).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token renovado correctamente"),
        @ApiResponse(responseCode = "400", description = "Refresh token inválido o expirado")
    })
    @SecurityRequirements  // usa refreshToken en body, no JWT en header
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authenticationService.refreshAccessToken(request.refreshToken()));
    }

    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token del usuario en la base de datos")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal User currentUser) {
        authenticationService.logout(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(hidden = true)  // ocultar endpoint de prueba en Swagger
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }
}

