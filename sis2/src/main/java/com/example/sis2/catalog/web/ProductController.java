package com.example.sis2.catalog.web;

import com.example.sis2.catalog.app.ProductService;
import com.example.sis2.catalog.domain.Product;
import com.example.sis2.catalog.web.dto.ProductRequest;
import com.example.sis2.catalog.web.dto.ProductResponse;
import com.example.sis2.catalog.web.dto.ReduceStockRequest;
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
    private final ProductApiMapper apiMapper;

    public ProductController(ProductService productService, ProductApiMapper apiMapper) {
        this.productService = productService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return productService.getProducts(categoryId, pageable)
                .map(apiMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return apiMapper.toResponse(product);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product created = productService.createProduct(
                request.name(),
                request.price(),
                request.stock(),
                request.categoryId()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(apiMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product updated = productService.updateProduct(
                id,
                request.name(),
                request.price(),
                request.stock(),
                request.categoryId()
        );
        return apiMapper.toResponse(updated);
    }

    @PatchMapping("/{id}/reduce-stock")
    public ProductResponse reduceStock(@PathVariable Long id, @Valid @RequestBody ReduceStockRequest request) {
        Product updated = productService.reduceStock(id, request.quantity());
        return apiMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
