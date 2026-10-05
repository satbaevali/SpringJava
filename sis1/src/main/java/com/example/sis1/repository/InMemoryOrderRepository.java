package com.example.sis1.repository;

import com.example.sis1.model.Order;
import com.example.sis1.model.OrderItem;
import com.example.sis1.model.OrderStatus;
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
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostConstruct
    public void initSampleData() {
        List<OrderItem> sampleItems = List.of(
                new OrderItem(1L, "MacBook Pro 14 M3", new BigDecimal("1999.99"), 1, new BigDecimal("1999.99")),
                new OrderItem(3L, "AirPods Pro 2", new BigDecimal("249.50"), 2, new BigDecimal("499.00"))
        );
        save(new Order(
                null,
                "Aidos Nurlanov",
                "aidos@example.com",
                OrderStatus.CONFIRMED,
                sampleItems,
                new BigDecimal("2498.99"),
                Instant.now()
        ));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGenerator.incrementAndGet());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(Instant.now());
        }
        storage.put(order.getId(), order);
        return order;
    }
    @Override
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}