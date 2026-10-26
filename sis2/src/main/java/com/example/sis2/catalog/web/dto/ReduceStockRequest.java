package com.example.sis2.catalog.web.dto;

import jakarta.validation.constraints.Positive;

public record ReduceStockRequest(
        @Positive(message = "Количество для списания должно быть больше 0")
        int quantity
) {}
