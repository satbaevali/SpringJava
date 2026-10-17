package com.example.practice4.web;

import com.example.practice4.dto.ProductRequest;
import com.example.practice4.dto.ProductResponse;
import com.example.practice4.dto.ReduceStockRequest;
import com.example.practice4.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ТРЕБОВАНИЕ ПРАКТИКИ (Слайды 27 и 30):
    // Эндпоинт со списком товаров с поддержкой пагинации и сортировки (Pageable)
    // Пример запроса: GET /api/products?page=0&size=10&sort=price,desc
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);

        // 201 Created с заголовком Location (слайд 28)
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // Списание остатка товара (демонстрация транзакции и оптимистичной блокировки @Version)
    @PostMapping("/{id}/reduce-stock")
    public ResponseEntity<ProductResponse> reduceStock(
            @PathVariable Long id,
            @Valid @RequestBody ReduceStockRequest request) {
        return ResponseEntity.ok(productService.reduceStock(id, request.quantity()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
