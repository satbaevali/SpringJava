package com.example.sis2.catalog.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByName(String name);
    boolean existsByName(String name);
}
