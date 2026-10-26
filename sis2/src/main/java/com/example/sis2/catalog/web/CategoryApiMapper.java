package com.example.sis2.catalog.web;

import com.example.sis2.catalog.domain.Category;
import com.example.sis2.catalog.web.dto.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryApiMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
