package com.example.practice4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Название товара обязательно")
        @Size(min = 2, max = 150, message = "Название должно быть от 2 до 150 символов")
        String name,

        @NotNull(message = "Цена обязательна")
        @Positive(message = "Цена должна быть больше нуля")
        BigDecimal price,

        @PositiveOrZero(message = "Количество не может быть отрицательным")
        int stock,

        @NotNull(message = "ID категории обязательно")
        Long categoryId
) {}
