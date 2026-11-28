package com.example.sis2.catalog.app;

import com.example.sis2.catalog.domain.Category;
import com.example.sis2.catalog.domain.CategoryRepository;
import com.example.sis2.common.exception.ResourceConflictException;
import com.example.sis2.common.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Cacheable(value = "categoryById", key = "#id")
    public Category getCategoryById(Long id) {
        return findCategoryOrThrow(id);
    }

    @Transactional
    @CacheEvict(value = {"categories", "categoryById"}, allEntries = true)
    public Category createCategory(String name, String description) {
        if (categoryRepository.existsByName(name.trim())) {
            throw new ResourceConflictException("Категория с именем '" + name + "' уже существует");
        }
        Category category = Category.createNew(name, description);
        return categoryRepository.save(category);
    }

    @Transactional
    @CacheEvict(value = {"categories", "categoryById", "productById"}, allEntries = true)
    public Category updateCategory(Long id, String name, String description) {
        Category existing = findCategoryOrThrow(id);
        if (!existing.getName().equalsIgnoreCase(name.trim()) && categoryRepository.existsByName(name.trim())) {
            throw new ResourceConflictException("Категория с именем '" + name + "' уже существует");
        }
        existing.updateDetails(name, description);
        return categoryRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = {"categories", "categoryById", "productById"}, allEntries = true)
    public void deleteCategory(Long id) {
        findCategoryOrThrow(id);
        categoryRepository.deleteById(id);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + id + " не найдена"));
    }
}
