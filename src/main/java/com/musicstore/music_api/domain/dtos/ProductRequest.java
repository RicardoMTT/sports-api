package com.musicstore.music_api.domain.dtos;

import com.musicstore.music_api.domain.enums.Category;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @NotBlank String name,

        @NotBlank(message = "La marca es obligatoria")
        @Size(min = 2, max = 60, message = "La marca debe tener entre 2 y 60 caracteres")
        @NotBlank String brand,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 enteros y 2 decimales")
        BigDecimal price,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        @Max(value = 10000, message = "El stock no puede superar 10,000 unidades")
        Integer stock,

        @NotNull(message = "La categoría es obligatoria")
        @NotNull Category category,

        @Size(max = 500, message = "La URL de imagen no puede superar 500 caracteres")
        String imageUrl
) {
}

