package com.sportsstore.sports_api.domain.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        @NotNull Long productId,
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @NotNull @Min(1) Integer quantity) {
}
