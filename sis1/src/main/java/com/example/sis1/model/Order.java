package com.example.sis1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String customerName;
    private String customerEmail;
    private OrderStatus status;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private Instant createdAt;
}