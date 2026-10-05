package com.example.sis1.repository;

import com.example.sis1.model.Category;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCategoryRepository implements CategoryRepository {

    // Потокобезопасная коллекция для хранения данных в памяти
    private final Map<Long, Category> storage = new ConcurrentHashMap<>();

    // Потокобезопасный генератор ID (имитирует AUTO_INCREMENT в базе данных)
    private final AtomicLong idGenerator = new AtomicLong(0);

    // Заполняем магазин начальными данными при старте
    @PostConstruct
    public void initSampleData() {
        save(new Category(null, "Laptops", "Portable computers and ultrabooks", Instant.now()));
        save(new Category(null, "Smartphones", "Mobile phones and accessories", Instant.now()));
        save(new Category(null, "Audio", "Headphones, speakers, and microphones", Instant.now()));
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Category> findByNameIgnoreCase(String name) {
        return storage.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            category.setId(idGenerator.incrementAndGet());
        }
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(Instant.now());
        }
        storage.put(category.getId(), category);
        return category;
    }

    @Override
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}