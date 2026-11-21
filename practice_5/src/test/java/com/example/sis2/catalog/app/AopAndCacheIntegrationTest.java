package com.example.sis2.catalog.app;

import com.example.sis2.catalog.domain.*;
import com.example.sis2.common.aop.ExecutionTimeAspect;
import com.example.sis2.common.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        CacheConfig.class,
        ExecutionTimeAspect.class,
        ProductService.class,
        CategoryService.class,
        StockAvailabilityPolicy.class
})
class AopAndCacheIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames()
                .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
        reset(productRepository, categoryRepository);
    }

    @Test
    @DisplayName("Practice 5 AOP: сервисы обернуты в Spring AOP прокси с ExecutionTimeAspect")
    void servicesAreWrappedInAopProxies() {
        assertThat(AopUtils.isAopProxy(productService)).isTrue();
        assertThat(AopUtils.isAopProxy(categoryService)).isTrue();
    }

    @Test
    @DisplayName("Practice 5 Cache")
    void productByIdIsCachedAndEvictedOnWrite() {
        Product product = new Product(1L, "MacBook Pro 16", Money.of("2499.99"), 10, 1L, "Электроника", 0L);
        when(productRepository.findById(1L)).thenAnswer(inv -> {
            Thread.sleep(50);
            return Optional.of(product);
        });
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        long startMiss = System.nanoTime();
        Product firstRead = productService.getProductById(1L);
        long firstCallMs = (System.nanoTime() - startMiss) / 1_000_000;

        long startHit = System.nanoTime();
        Product secondRead = productService.getProductById(1L);
        long secondCallMs = (System.nanoTime() - startHit) / 1_000_000;

        assertThat(firstRead.getName()).isEqualTo("MacBook Pro 16");
        assertThat(secondRead.getName()).isEqualTo("MacBook Pro 16");
        assertThat(firstCallMs).isGreaterThanOrEqualTo(45L);
        assertThat(secondCallMs).isLessThan(15L);
        verify(productRepository, times(1)).findById(1L);

        productService.reduceStock(1L, 2);

        productService.getProductById(1L);
        verify(productRepository, times(3)).findById(1L);
    }

    @Test
    @DisplayName("Practice 5 Cache:")
    void categoriesAreCachedAndEvictedOnCreate() {
        Category cat = new Category(1L, "Электроника", "Гаджеты");
        when(categoryRepository.findAll()).thenAnswer(inv -> {
            Thread.sleep(40);
            return List.of(cat);
        });
        when(categoryRepository.existsByName("Книги")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category(2L, "Книги", "Литература"));

        categoryService.getAllCategories();
        categoryService.getAllCategories();
        verify(categoryRepository, times(1)).findAll();

        categoryService.createCategory("Книги", "Литература");

        categoryService.getAllCategories();
        verify(categoryRepository, times(2)).findAll();
    }
}
