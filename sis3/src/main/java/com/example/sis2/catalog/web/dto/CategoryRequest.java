package com.example.sis2.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление категории")
public record CategoryRequest(
        @Schema(description = "Название категории", example = "Электроника")
        @NotBlank(message = "Название категории не может быть пустым")
        @Size(max = 100, message = "Название категории не должно превышать 100 символов")
        String name,

        @Schema(description = "Описание категории", example = "Ноутбуки, смартфоны и аксессуары")
        @Size(max = 500, message = "Описание не должно превышать 500 символов")
        String description
) {}
