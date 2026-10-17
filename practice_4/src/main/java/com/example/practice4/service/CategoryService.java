package com.example.practice4.service;

import com.example.practice4.dto.CategoryRequest;
import com.example.practice4.dto.CategoryResponse;
import com.example.practice4.exception.ResourceConflictException;
import com.example.practice4.exception.ResourceNotFoundException;
import com.example.practice4.model.Category;
import com.example.practice4.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Чтение: readOnly = true оптимизирует сессию Hibernate (слайд 16 и 30)
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с id=" + id + " не найдена"));
        return toResponse(category);
    }

    // Запись: полноценная транзакция @Transactional (слайд 13 и 30)
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ResourceConflictException("Категория с именем '" + request.name() + "' уже существует");
        }
        Category category = new Category(request.name(), request.description());
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с id=" + id + " не найдена"));

        // Если имя меняется, проверяем на уникальность
        if (!category.getName().equalsIgnoreCase(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new ResourceConflictException("Категория с именем '" + request.name() + "' уже существует");
        }

        // Dirty Checking (слайд 23): меняем поля в Managed объекте,
        // Hibernate автоматически сгенерирует UPDATE при коммите транзакции!
        category.setName(request.name());
        category.setDescription(request.description());

        return toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категория с id=" + id + " не найдена");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
