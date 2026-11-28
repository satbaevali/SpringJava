package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryEntityMapper {

    public Category toDomain(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Category(entity.getId(), entity.getName(), entity.getDescription());
    }

    public CategoryEntity toEntity(Category domain) {
        if (domain == null) {
            return null;
        }
        return new CategoryEntity(domain.getId(), domain.getName(), domain.getDescription());
    }
}
