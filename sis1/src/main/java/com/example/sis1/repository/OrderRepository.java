package com.example.sis1.repository;

import com.example.sis1.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findAll();
    Optional<Order> findById(Long id);
    Order save(Order order);
    boolean deleteById(Long id);
}