package com.sportsstore.sports_api.domain.dtos;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email
) {}
