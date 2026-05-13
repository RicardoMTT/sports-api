package com.musicstore.music_api.domain.dtos;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        String productBrand,
        Integer quantity,
        BigDecimal unitPrice,     // precio congelado al momento de la compra
        BigDecimal subtotal
) {
}

