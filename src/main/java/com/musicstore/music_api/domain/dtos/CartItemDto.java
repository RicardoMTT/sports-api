package com.musicstore.music_api.domain.dtos;

import java.math.BigDecimal;

public record CartItemDto(Long productId, String productName, Integer quantity, BigDecimal subtotal) {
}

