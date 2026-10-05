package com.example.sis1.repository;

import com.example.sis1.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostConstruct
    public void initSampleData() {
        save(new Product(null, "LAPTOP-001", "MacBook Pro 14 M3",
                "14-inch Apple MacBook Pro, 16GB RAM, 512GB SSD",
                new BigDecimal("1999.99"), 10, 1L, Instant.now()));
        save(new Product(null, "PHONE-001", "iPhone 16 Pro",
                "128GB Natural Titanium smartphone",
                new BigDecimal("999.00"), 25, 2L, Instant.now()));
        save(new Product(null, "AUDIO-001", "AirPods Pro 2",
                "Active Noise Cancelling wireless earbuds",
                new BigDecimal("249.50"), 40, 3L, Instant.now()));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return storage.values().stream()
                .filter(p -> p.getCategoryId().equals(categoryId))
                .toList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Product> findBySkuIgnoreCase(String sku) {
        return storage.values().stream()
                .filter(p -> p.getSku().equalsIgnoreCase(sku))
                .findFirst();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.incrementAndGet());
        }
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(Instant.now());
        }
        storage.put(product.getId(), product);
        return product;
    }

    @Override
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return storage.values().stream()
                .anyMatch(p -> p.getCategoryId().equals(categoryId));
    }
}