package com.example.practice4;

import com.example.practice4.dto.ProductResponse;
import com.example.practice4.exception.InsufficientStockException;
import com.example.practice4.model.Category;
import com.example.practice4.model.Product;
import com.example.practice4.repository.CategoryRepository;
import com.example.practice4.repository.ProductRepository;
import com.example.practice4.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class Practice4ApplicationTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Контекст Spring Boot успешно поднимается и соединяется с PostgreSQL")
    void contextLoads() {
        assertThat(categoryRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(productRepository.count()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Derived Query: Поиск категории по имени")
    void testDerivedQueryFindByName() {
        Optional<Category> category = categoryRepository.findByName("Электроника");
        assertThat(category).isPresent();
        assertThat(category.get().getName()).isEqualTo("Электроника");
    }

    @Test
    @DisplayName("Derived Query + Pageable: Пагинация товаров по категории")
    void testDerivedQueryWithPagination() {
        Category category = categoryRepository.findByName("Электроника").orElseThrow();
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("price").descending());

        Page<Product> pagedProducts = productRepository.findByCategoryId(category.getId(), pageRequest);

        assertThat(pagedProducts).isNotNull();
        assertThat(pagedProducts.getContent()).hasSize(2);
        assertThat(pagedProducts.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("@Query c JOIN FETCH: Загрузка товара вместе с категорией за 1 запрос")
    void testJoinFetchQuery() {
        Product firstProduct = productRepository.findAll().get(0);

        Optional<Product> productOpt = productRepository.findByIdWithCategory(firstProduct.getId());
        assertThat(productOpt).isPresent();

        Product product = productOpt.get();
        assertThat(product.getCategory()).isNotNull();
        assertThat(product.getCategory().getName()).isNotBlank();
    }

    @Test
    @DisplayName("Транзакционное списание остатка и защита от перерасхода (@Version)")
    void testReduceStockAndConcurrencyCheck() {
        Product product = productRepository.findAll().get(0);
        int initialStock = product.getStock();

        ProductResponse response = productService.reduceStock(product.getId(), 1);
        assertThat(response.stock()).isEqualTo(initialStock - 1);

        assertThatThrownBy(() -> productService.reduceStock(product.getId(), 99999))
                .isInstanceOf(InsufficientStockException.class);
    }
}
