package com.example.sis1.service;

import com.example.sis1.dto.OrderItemRequest;
import com.example.sis1.dto.OrderItemResponse;
import com.example.sis1.dto.OrderRequest;
import com.example.sis1.dto.OrderResponse;
import com.example.sis1.dto.OrderStatusUpdateRequest;
import com.example.sis1.exception.ResourceConflictException;
import com.example.sis1.exception.ResourceNotFoundException;
import com.example.sis1.model.Order;
import com.example.sis1.model.OrderItem;
import com.example.sis1.model.OrderStatus;
import com.example.sis1.model.Product;
import com.example.sis1.repository.OrderRepository;
import com.example.sis1.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id=" + id + " not found"));
        return toResponse(order);
    }

    public OrderResponse create(OrderRequest request) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product with id=" + itemReq.productId() + " not found"));

            if (product.getStockQuantity() < itemReq.quantity()) {
                throw new ResourceConflictException(
                        "Insufficient stock for product '" + product.getName() + "' (id=" + product.getId()
                                + "): requested=" + itemReq.quantity()
                                + ", available=" + product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());
            productRepository.save(product);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalAmount = totalAmount.add(subtotal);

            orderItems.add(new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    itemReq.quantity(),
                    subtotal
            ));
        }

        Order order = new Order(
                null,
                request.customerName().trim(),
                request.customerEmail().trim(),
                OrderStatus.NEW,
                orderItems,
                totalAmount,
                Instant.now()
        );
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id=" + id + " not found"));

        if (existing.getStatus() == OrderStatus.DELIVERED || existing.getStatus() == OrderStatus.CANCELLED) {
            throw new ResourceConflictException(
                    "Cannot change status of order id=" + id + " because it is already " + existing.getStatus());
        }

        existing.setStatus(request.status());
        return toResponse(orderRepository.save(existing));
    }

    public void delete(Long id) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id=" + id + " not found"));

        if (existing.getStatus() == OrderStatus.SHIPPED || existing.getStatus() == OrderStatus.DELIVERED) {
            throw new ResourceConflictException(
                    "Cannot delete order id=" + id + " because it has already been " + existing.getStatus());
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus(),
                itemResponses,
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}
