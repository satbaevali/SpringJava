package com.example.practice4.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        int stock,
        Long categoryId,
        String categoryName,
        Long version
) {}
