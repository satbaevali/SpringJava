package com.example.sis2.catalog.domain;

import com.example.sis2.common.exception.InvalidDomainRuleException;
import java.util.Objects;

public class Category {

    private final Long id;
    private String name;
    private String description;

    public Category(Long id, String name, String description) {
        validateName(name);
        this.id = id;
        this.name = name.trim();
        this.description = description;
    }

    public static Category createNew(String name, String description) {
        return new Category(null, name, description);
    }

    public void updateDetails(String newName, String newDescription) {
        validateName(newName);
        this.name = newName.trim();
        this.description = newDescription;
    }

    private void validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainRuleException("Название категории не может быть пустым");
        }
        if (value.trim().length() > 100) {
            throw new InvalidDomainRuleException("Название категории не может превышать 100 символов");
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
