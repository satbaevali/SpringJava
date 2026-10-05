package com.example.sis1.service;

import com.example.sis1.dto.CategoryRequest;
import com.example.sis1.dto.CategoryResponse;
import com.example.sis1.exception.ResourceConflictException;
import com.example.sis1.exception.ResourceNotFoundException;
import com.example.sis1.model.Category;
import com.example.sis1.repository.CategoryRepository;
import com.example.sis1.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id=" + id + " not found"));
        return toResponse(category);
    }

    public CategoryResponse create(CategoryRequest request) {
        categoryRepository.findByNameIgnoreCase(request.name().trim())
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Category with name '" + request.name() + "' already exists");
                });

        Category category = new Category(null, request.name().trim(), request.description(), Instant.now());
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id=" + id + " not found"));

        categoryRepository.findByNameIgnoreCase(request.name().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResourceConflictException("Category with name '" + request.name() + "' already exists");
                });

        existing.setName(request.name().trim());
        existing.setDescription(request.description());
        return toResponse(categoryRepository.save(existing));
    }

    public void delete(Long id) {
        if (categoryRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Category with id=" + id + " not found");
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new ResourceConflictException(
                    "Cannot delete category with id=" + id + " because it still contains products");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt()
        );
    }
}
