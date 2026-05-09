package com.sportsstore.sports_api.domain.dtos;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}
