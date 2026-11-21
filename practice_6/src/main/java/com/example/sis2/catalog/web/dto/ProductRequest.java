package com.example.sis2.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Запрос на создание или обновление товара")
public record ProductRequest(
        @Schema(description = "Название товара", example = "MacBook Pro 16")
        @NotBlank(message = "Название товара обязательно")
        @Size(max = 150, message = "Название товара не может превышать 150 символов")
        String name,

        @Schema(description = "Цена товара в USD", example = "2499.99")
        @NotNull(message = "Цена товара обязательна")
        @Positive(message = "Цена товара должна быть больше 0")
        BigDecimal price,

        @Schema(description = "Количество на складе", example = "15")
        @PositiveOrZero(message = "Остаток на складе не может быть отрицательным")
        int stock,

        @Schema(description = "ID категории", example = "1")
        @NotNull(message = "ID категории обязателен")
        Long categoryId
) {}
