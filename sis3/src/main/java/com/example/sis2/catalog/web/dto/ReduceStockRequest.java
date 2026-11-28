package com.example.sis2.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Запрос на списание остатка товара со склада")
public record ReduceStockRequest(
        @Schema(description = "Количество единиц для списания", example = "2")
        @Positive(message = "Количество для списания должно быть больше 0")
        int quantity
) {}
