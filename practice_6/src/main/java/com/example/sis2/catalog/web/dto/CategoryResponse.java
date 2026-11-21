package com.example.sis2.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными категории")
public record CategoryResponse(
        @Schema(description = "ID категории", example = "1")
        Long id,
        @Schema(description = "Название категории", example = "Электроника")
        String name,
        @Schema(description = "Описание категории", example = "Ноутбуки, смартфоны и аксессуары")
        String description
) {}
