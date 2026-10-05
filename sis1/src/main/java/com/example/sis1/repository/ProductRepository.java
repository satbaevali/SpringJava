package com.example.sis1.repository;

import com.example.sis1.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    List<Product> findByCategoryId(Long categoryId);
    Optional<Product> findById(Long id);
    Optional<Product> findBySkuIgnoreCase(String sku);
    Product save(Product product);
    boolean deleteById(Long id);
    boolean existsByCategoryId(Long categoryId);
}