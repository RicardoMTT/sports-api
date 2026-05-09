package com.sportsstore.sports_api.domain.dtos;

public record AuthenticationResponse(
        String token,
        String refreshToken,    // refresh token (7 días)
        UserDto user            // datos del usuario para el frontend
) {

    // Constructor de compatibilidad para no romper código existente
    public AuthenticationResponse(String token) {
        this(token, null, null);
    }
}
