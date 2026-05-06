package com.sportsstore.sports_api.domain.dtos;

import com.sportsstore.sports_api.domain.enums.Category;

import java.math.BigDecimal;

public record ProductResponse(
        Long id, String name, String brand, BigDecimal price, Integer stock, Category category,
        String imageUrl
) {
}
