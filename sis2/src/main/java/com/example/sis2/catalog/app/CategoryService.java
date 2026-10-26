package com.example.sis2.catalog.app;

import com.example.sis2.catalog.domain.Category;
import com.example.sis2.catalog.domain.CategoryRepository;
import com.example.sis2.common.exception.ResourceConflictException;
import com.example.sis2.common.exception.ResourceNotFoundException;
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

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + id + " не найдена"));
    }

    @Transactional
    public Category createCategory(String name, String description) {
        if (categoryRepository.existsByName(name.trim())) {
            throw new ResourceConflictException("Категория с именем '" + name + "' уже существует");
        }
        Category category = Category.createNew(name, description);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description) {
        Category existing = getCategoryById(id);
        if (!existing.getName().equalsIgnoreCase(name.trim()) && categoryRepository.existsByName(name.trim())) {
            throw new ResourceConflictException("Категория с именем '" + name + "' уже существует");
        }
        existing.updateDetails(name, description);
        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        getCategoryById(id);
        categoryRepository.deleteById(id);
    }
}
