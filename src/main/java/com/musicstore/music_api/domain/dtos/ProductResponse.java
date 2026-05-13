package com.musicstore.music_api.domain.dtos;

import com.musicstore.music_api.domain.enums.Category;

import java.math.BigDecimal;

public record ProductResponse(
        Long id, String name, String brand, BigDecimal price, Integer stock, Category category,
        String imageUrl
) {
}

