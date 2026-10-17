package com.example.practice4.repository;

import com.example.practice4.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Derived Query: поиск по имени (слайд 25)
    Optional<Category> findByName(String name);

    // Derived Query: проверка существования (слайд 25)
    boolean existsByName(String name);
}
