package com.example.sis2.catalog.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Название товара обязательно")
        @Size(max = 150, message = "Название товара не может превышать 150 символов")
        String name,

        @NotNull(message = "Цена товара обязательна")
        @Positive(message = "Цена товара должна быть больше 0")
        BigDecimal price,

        @PositiveOrZero(message = "Остаток на складе не может быть отрицательным")
        int stock,

        @NotNull(message = "ID категории обязателен")
        Long categoryId
) {}
