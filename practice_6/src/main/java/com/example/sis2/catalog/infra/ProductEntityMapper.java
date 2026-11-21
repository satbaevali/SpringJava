package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.Money;
import com.example.sis2.catalog.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityMapper {

    public Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Product(
                entity.getId(),
                entity.getName(),
                Money.of(entity.getPrice()),
                entity.getStock(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getVersion()
        );
    }

    public ProductEntity toEntity(Product domain, CategoryEntity categoryEntity) {
        if (domain == null) {
            return null;
        }
        return new ProductEntity(
                domain.getId(),
                domain.getName(),
                domain.getPrice().amount(),
                domain.getStock(),
                categoryEntity,
                domain.getVersion()
        );
    }
}
