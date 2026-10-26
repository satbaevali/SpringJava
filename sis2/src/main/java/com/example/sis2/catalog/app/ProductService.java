package com.example.sis2.catalog.app;

import com.example.sis2.catalog.domain.*;
import com.example.sis2.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockAvailabilityPolicy stockPolicy;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            StockAvailabilityPolicy stockPolicy) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockPolicy = stockPolicy;
    }

    public Page<Product> getProducts(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return productRepository.findByCategoryId(categoryId, pageable);
        }
        return productRepository.findAll(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар с ID " + id + " не найден"));
    }

    @Transactional
    public Product createProduct(String name, BigDecimal price, int stock, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + categoryId + " не найдена"));

        Product product = Product.createNew(name, Money.of(price), stock, category);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, String name, BigDecimal price, int stock, Long categoryId) {
        Product product = getProductById(id);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + categoryId + " не найдена"));

        product.updateDetails(name, Money.of(price), stock, category);
        return productRepository.save(product);
    }

    @Transactional
    public Product reduceStock(Long id, int quantity) {
        Product product = getProductById(id);
        stockPolicy.validateBulkReduction(product, quantity);
        product.reduceStock(quantity);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Товар с ID " + id + " не найден");
        }
        productRepository.deleteById(id);
    }
}
