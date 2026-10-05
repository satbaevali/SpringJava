package com.example.sis1.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String sku;             // Артикул товара (например, LAPTOP-001)
    private String name;            // Название товара
    private String description;     // Описание
    private BigDecimal price;       // Цена (в Java для денег ВСЕГДА используют BigDecimal, а не double!)
    private Integer stockQuantity;  // Количество на складе
    private Long categoryId;        // ID категории, к которой относится товар
    private Instant createdAt;
}