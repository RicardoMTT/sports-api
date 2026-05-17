package com.musicstore.music_api.controller;

import com.musicstore.music_api.domain.dtos.ChangePasswordRequest;
import com.musicstore.music_api.domain.dtos.UpdateProfileRequest;
import com.musicstore.music_api.domain.dtos.UserProfileResponse;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Perfil", description = "Consulta y edición del perfil del usuario autenticado")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Ver mi perfil", description = "Retorna los datos del usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getProfile(currentUser.getId()));
    }

    @Operation(summary = "Actualizar perfil", description = "Permite cambiar nombre y apellido del usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), request));
    }

    @Operation(summary = "Cambiar contraseña", description = "Requiere la contraseña actual para confirmar la identidad antes de cambiarla")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Contraseña cambiada correctamente"),
        @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva contraseña inválida"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
