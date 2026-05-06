package com.sportsstore.sports_api.domain.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long cartId, List<CartItemDto> items, BigDecimal totalPrice) {
}
