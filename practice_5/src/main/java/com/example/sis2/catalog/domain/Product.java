package com.example.sis2.catalog.domain;

import com.example.sis2.common.exception.InsufficientStockException;
import com.example.sis2.common.exception.InvalidDomainRuleException;
import java.util.Objects;

public class Product {

    private final Long id;
    private String name;
    private Money price;
    private int stock;
    private Long categoryId;
    private String categoryName;
    private final Long version;

    public Product(Long id, String name, Money price, int stock, Long categoryId, String categoryName, Long version) {
        validateInvariants(name, price, stock, categoryId);
        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.version = version;
    }

    public static Product createNew(String name, Money price, int stock, Category category) {
        Objects.requireNonNull(category, "Категория обязательна для создания товара");
        return new Product(null, name, price, stock, category.getId(), category.getName(), 0L);
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidDomainRuleException("Количество для списания должно быть положительным: " + quantity);
        }
        if (this.stock < quantity) {
            throw new InsufficientStockException(
                    "Недостаточно товара '" + this.name + "' на складе! Доступно: " + this.stock + ", запрошено: " + quantity);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidDomainRuleException("Количество для пополнения должно быть положительным: " + quantity);
        }
        this.stock += quantity;
    }

    public void updateDetails(String newName, Money newPrice, int newStock, Category category) {
        Objects.requireNonNull(category, "Категория обязательна");
        validateInvariants(newName, newPrice, newStock, category.getId());
        this.name = newName.trim();
        this.price = newPrice;
        this.stock = newStock;
        this.categoryId = category.getId();
        this.categoryName = category.getName();
    }

    private void validateInvariants(String name, Money price, int stock, Long categoryId) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainRuleException("Название товара не может быть пустым");
        }
        if (price == null) {
            throw new InvalidDomainRuleException("Цена товара обязательна");
        }
        if (stock < 0) {
            throw new InvalidDomainRuleException("Остаток на складе не может быть отрицательным: " + stock);
        }
        if (categoryId == null) {
            throw new InvalidDomainRuleException("ID категории обязателен");
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Money getPrice() { return price; }
    public int getStock() { return stock; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Long getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
