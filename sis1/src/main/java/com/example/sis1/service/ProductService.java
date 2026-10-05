package com.example.sis1.service;

import com.example.sis1.dto.ProductRequest;
import com.example.sis1.dto.ProductResponse;
import com.example.sis1.exception.ResourceConflictException;
import com.example.sis1.exception.ResourceNotFoundException;
import com.example.sis1.model.Category;
import com.example.sis1.model.Product;
import com.example.sis1.repository.CategoryRepository;
import com.example.sis1.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponse> findAll(Long categoryId) {
        List<Product> products = (categoryId == null)
                ? productRepository.findAll()
                : productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id=" + id + " not found"));
        return toResponse(product);
    }

    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category with id=" + request.categoryId() + " not found"));

        productRepository.findBySkuIgnoreCase(request.sku().trim())
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Product with SKU '" + request.sku() + "' already exists");
                });

        Product product = new Product(
                null,
                request.sku().trim(),
                request.name().trim(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                category.getId(),
                Instant.now()
        );
        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id=" + id + " not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category with id=" + request.categoryId() + " not found"));

        productRepository.findBySkuIgnoreCase(request.sku().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResourceConflictException("Product with SKU '" + request.sku() + "' already exists");
                });

        existing.setSku(request.sku().trim());
        existing.setName(request.name().trim());
        existing.setDescription(request.description());
        existing.setPrice(request.price());
        existing.setStockQuantity(request.stockQuantity());
        existing.setCategoryId(category.getId());

        return toResponse(productRepository.save(existing));
    }

    public void delete(Long id) {
        if (!productRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Product with id=" + id + " not found");
        }
    }

    private ProductResponse toResponse(Product product) {
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategoryId(),
                categoryName,
                product.getCreatedAt()
        );
    }
}
