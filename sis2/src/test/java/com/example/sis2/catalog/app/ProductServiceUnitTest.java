package com.example.sis2.catalog.app;

import com.example.sis2.catalog.domain.*;
import com.example.sis2.common.exception.InsufficientStockException;
import com.example.sis2.common.exception.InvalidDomainRuleException;
import com.example.sis2.common.exception.ResourceConflictException;
import com.example.sis2.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private StockAvailabilityPolicy stockPolicy;
    private ProductService productService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        stockPolicy = new StockAvailabilityPolicy();
        productService = new ProductService(productRepository, categoryRepository, stockPolicy);
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    @DisplayName("Value Object Money: валидирует положительную цену и округляет до 2 знаков")
    void moneyValueObjectValidatesAndScales() {
        Money validMoney = Money.of(new BigDecimal("199.995"));
        assertThat(validMoney.amount()).isEqualByComparingTo("200.00");

        assertThatThrownBy(() -> Money.of(BigDecimal.ZERO))
                .isInstanceOf(InvalidDomainRuleException.class)
                .hasMessageContaining("больше нуля");

        assertThatThrownBy(() -> Money.of(new BigDecimal("-10.00")))
                .isInstanceOf(InvalidDomainRuleException.class);
    }

    @Test
    @DisplayName("Rich Domain Product: защищает инвариант остатка на складе")
    void domainProductGuardsStockInvariants() {
        Category electronics = new Category(1L, "Электроника", "Гаджеты");
        Product product = Product.createNew("MacBook Pro", Money.of("1999.99"), 5, electronics);

        product.reduceStock(3);
        assertThat(product.getStock()).isEqualTo(2);

        assertThatThrownBy(() -> product.reduceStock(10))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Недостаточно товара");
    }

    @Test
    @DisplayName("ProductService.createProduct: оркестрирует поиск категории и сохранение агрегата")
    void createProductSuccess() {
        Category category = new Category(1L, "Электроника", "Гаджеты");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            return new Product(10L, p.getName(), p.getPrice(), p.getStock(), p.getCategoryId(), p.getCategoryName(), 0L);
        });

        Product created = productService.createProduct("iPad Air", new BigDecimal("699.00"), 12, 1L);

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getName()).isEqualTo("iPad Air");
        assertThat(created.getCategoryName()).isEqualTo("Электроника");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("ProductService.createProduct: выбрасывает 404 если категория не существует")
    void createProductWhenCategoryNotFoundThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct("iPad Air", new BigDecimal("699.00"), 12, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("ProductService.reduceStock: проверяет политику StockAvailabilityPolicy и списывает остаток")
    void reduceStockChecksPolicyAndUpdatesDomain() {
        Product existing = new Product(1L, "AirPods", Money.of("199.00"), 20, 1L, "Электроника", 0L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = productService.reduceStock(1L, 5);
        assertThat(updated.getStock()).isEqualTo(15);

        assertThatThrownBy(() -> productService.reduceStock(1L, 1000))
                .isInstanceOf(InvalidDomainRuleException.class)
                .hasMessageContaining("Превышен лимит");
    }

    @Test
    @DisplayName("CategoryService.createCategory: запрещает создание дубликата категории (409 Conflict)")
    void createDuplicateCategoryThrowsConflict() {
        when(categoryRepository.existsByName("Электроника")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory("Электроника", "Описание"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("уже существует");

        verify(categoryRepository, never()).save(any());
    }
}
