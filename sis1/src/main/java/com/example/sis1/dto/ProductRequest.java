package com.example.sis1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "SKU must not be blank")
        @Pattern(regexp = "^[A-Z0-9-]{3,20}$", message = "SKU must match pattern ^[A-Z0-9-]{3,20}$ (e.g., LAPTOP-001)")
        String sku,

        @NotBlank(message = "Product name must not be blank")
        @Size(min = 2, max = 120, message = "Product name must be between 2 and 120 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity must be 0 or greater")
        Integer stockQuantity,

        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be a positive number")
        Long categoryId
) {
}