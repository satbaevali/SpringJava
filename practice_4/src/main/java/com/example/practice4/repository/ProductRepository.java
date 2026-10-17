package com.example.practice4.repository;

import com.example.practice4.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Derived Query с пагинацией (слайды 25 и 27)
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // 2. Derived Query: товары, которые есть в наличии (слайд 25)
    List<Product> findByStockGreaterThan(int minStock);

    // 3. @Query c JOIN FETCH: избегаем N+1 проблемы при выборке с категорией (слайды 22 и 26)
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    // 4. @Query c JOIN FETCH и поддержкой пагинации (слайды 26 и 27)
    @Query(
        value = "SELECT p FROM Product p JOIN FETCH p.category",
        countQuery = "SELECT count(p) FROM Product p"
    )
    Page<Product> findAllWithCategory(Pageable pageable);

    // 5. @Modifying атомарное списание остатка напрямую через SQL (слайды 10 и 26)
    // Возвращает количество обновленных строк (0 = товара не хватило)
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int reduceStockAtomic(@Param("id") Long id, @Param("quantity") int quantity);
}
