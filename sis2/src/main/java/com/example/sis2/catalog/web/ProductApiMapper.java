package com.example.sis2.catalog.web;

import com.example.sis2.catalog.domain.Product;
import com.example.sis2.catalog.web.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductApiMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice().amount(),
                product.getStock(),
                product.getCategoryId(),
                product.getCategoryName(),
                product.getVersion()
        );
    }
}
