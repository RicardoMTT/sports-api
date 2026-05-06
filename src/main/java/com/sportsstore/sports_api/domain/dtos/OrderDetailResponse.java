package com.sportsstore.sports_api.domain.dtos;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Version completa - para el detalle de una orden
public record OrderDetailResponse(
        Long orderId,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
