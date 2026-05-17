package com.musicstore.music_api.domain.dtos;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        LocalDateTime memberSince
) {}
