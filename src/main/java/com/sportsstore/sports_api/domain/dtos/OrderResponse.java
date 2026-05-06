package com.sportsstore.sports_api.domain.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId, String status, BigDecimal totalAmount,
        LocalDateTime createdAt        // ← nuevo campo
) {

    // Constructor de compatibilidad para el checkout existente en ShoppingController
    public OrderResponse(Long orderId, String status, BigDecimal totalAmount) {
        this(orderId, status, totalAmount, null);
    }
}
