package com.example.sis1.repository;

import com.example.sis1.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findAll();
    Optional<Category> findById(Long id);
    Optional<Category> findByNameIgnoreCase(String name);
    Category save(Category category);
    boolean deleteById(Long id);
}