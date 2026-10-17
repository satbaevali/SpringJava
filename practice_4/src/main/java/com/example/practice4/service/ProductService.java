package com.example.practice4.service;

import com.example.practice4.dto.ProductRequest;
import com.example.practice4.dto.ProductResponse;
import com.example.practice4.exception.InsufficientStockException;
import com.example.practice4.exception.ResourceNotFoundException;
import com.example.practice4.model.Category;
import com.example.practice4.model.Product;
import com.example.practice4.repository.CategoryRepository;
import com.example.practice4.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Пагинация списка товаров с JOIN FETCH (слайды 26, 27, 30)
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Long categoryId, Pageable pageable) {
        Page<Product> page;
        if (categoryId != null) {
            page = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            page = productRepository.findAllWithCategory(pageable);
        }
        return page.map(this::toResponse);
    }

    // Получение по ID с JOIN FETCH (избегаем LazyInitializationException, слайд 22)
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар с id=" + id + " не найден"));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория с id=" + request.categoryId() + " не найдена"));

        Product product = new Product(
                request.name(),
                request.price(),
                request.stock(),
                category
        );

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар с id=" + id + " не найден"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория с id=" + request.categoryId() + " не найдена"));

        // Dirty Checking (слайд 23): меняем поля, Hibernate сам отправит UPDATE в БД
        product.setName(request.name());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(category);

        return toResponse(product);
    }

    // Списание остатка с защитой @Version (слайды 19, 28)
    @Transactional
    public ProductResponse reduceStock(Long id, int quantity) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар с id=" + id + " не найден"));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Недостаточно товара '" + product.getName() + "' на складе. Доступно: " + product.getStock()
            );
        }

        // Вызываем бизнес-метод сущности: меняет поле в памяти,
        // а при коммите Hibernate проверит @Version (UPDATE product SET stock=?, version=? WHERE id=? AND version=?)
        product.reduceStock(quantity);

        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Товар с id=" + id + " не найден");
        }
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getVersion()
        );
    }
}
