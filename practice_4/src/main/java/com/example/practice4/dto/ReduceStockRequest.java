package com.example.practice4.dto;

import jakarta.validation.constraints.Positive;

public record ReduceStockRequest(
        @Positive(message = "Количество для списания должно быть больше нуля")
        int quantity
) {}
