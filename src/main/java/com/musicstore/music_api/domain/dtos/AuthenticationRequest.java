package com.musicstore.music_api.domain.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank(message = "El correo es obligatorio") @Email String email,
        @NotBlank(message = "La contraseña es obligatoria") String password
) {
}

