package com.musicstore.music_api.domain.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio") String firstName,
        @NotBlank(message = "El apellido es obligatorio") String lastName,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Formato de correo inválido") String email,
        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password
) {
}

