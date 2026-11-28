package com.example.sis2.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ответ с данными товара")
public record ProductResponse(
        @Schema(description = "ID товара", example = "1")
        Long id,
        @Schema(description = "Название товара", example = "MacBook Pro 16")
        String name,
        @Schema(description = "Цена товара в USD", example = "2499.99")
        BigDecimal price,
        @Schema(description = "Остаток на складе", example = "15")
        int stock,
        @Schema(description = "ID категории", example = "1")
        Long categoryId,
        @Schema(description = "Название категории", example = "Электроника")
        String categoryName,
        @Schema(description = "Версия записи (Optimistic Locking)", example = "0")
        Long version
) {}
