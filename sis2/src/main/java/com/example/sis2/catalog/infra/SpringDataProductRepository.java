package com.example.sis2.catalog.infra;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "category")
    Page<ProductEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<ProductEntity> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.category WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithCategory(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock - :qty, p.version = p.version + 1 WHERE p.id = :id AND p.stock >= :qty")
    int reduceStockAtomically(@Param("id") Long id, @Param("qty") int quantity);
}
