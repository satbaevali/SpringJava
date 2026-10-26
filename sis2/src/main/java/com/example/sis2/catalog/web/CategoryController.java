package com.example.sis2.catalog.web;

import com.example.sis2.catalog.app.CategoryService;
import com.example.sis2.catalog.domain.Category;
import com.example.sis2.catalog.web.dto.CategoryRequest;
import com.example.sis2.catalog.web.dto.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryApiMapper apiMapper;

    public CategoryController(CategoryService categoryService, CategoryApiMapper apiMapper) {
        this.categoryService = categoryService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(apiMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return apiMapper.toResponse(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category created = categoryService.createCategory(request.name(), request.description());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(apiMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category updated = categoryService.updateCategory(id, request.name(), request.description());
        return apiMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
