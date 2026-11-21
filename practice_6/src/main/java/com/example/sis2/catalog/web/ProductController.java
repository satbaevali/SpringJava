package com.example.sis2.catalog.web;

import com.example.sis2.catalog.app.ProductService;
import com.example.sis2.catalog.domain.Product;
import com.example.sis2.catalog.web.dto.ProductRequest;
import com.example.sis2.catalog.web.dto.ProductResponse;
import com.example.sis2.catalog.web.dto.ReduceStockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/products", produces = "application/json;charset=UTF-8")
@Tag(name = "Products", description = "Управление товарами и расчёт стоимости в валютах")
public class ProductController {

    private final ProductService productService;
    private final ProductApiMapper apiMapper;

    public ProductController(ProductService productService, ProductApiMapper apiMapper) {
        this.productService = productService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    @Operation(summary = "Получить страницу товаров", description = "Возвращает пагинированный список товаров с опциональной фильтрацией по ID категории.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница товаров успешно получена")
    })
    public Page<ProductResponse> getProducts(
            @Parameter(description = "ID категории для фильтрации", example = "1")
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return productService.getProducts(categoryId, pageable)
                .map(apiMapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить товар по ID", description = "Возвращает детальную информацию о товаре (с кэшированием @Cacheable).")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар найден",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ProductResponse getProductById(
            @Parameter(description = "ID товара", example = "1") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return apiMapper.toResponse(product);
    }

    @PostMapping
    @Operation(summary = "Создать новый товар", description = "Создаёт новый товар в указанной категории и сбрасывает кэш.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Товар успешно создан",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Категория не найдена",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Нарушение бизнес-правила домена",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
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
    @Operation(summary = "Обновить товар", description = "Полностью обновляет данные товара по его ID и очищает кэш.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар успешно обновлён",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар или категория не найдены",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ProductResponse updateProduct(
            @Parameter(description = "ID товара", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
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
    @Operation(summary = "Списать остаток товара со склада", description = "Уменьшает остаток товара с проверкой доменной политики и инвариантов.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Остаток успешно списан",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Недостаточно товара на складе",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Превышен лимит разового списания (Domain Policy)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ProductResponse reduceStock(
            @Parameter(description = "ID товара", example = "1") @PathVariable Long id,
            @Valid @RequestBody ReduceStockRequest request) {
        Product updated = productService.reduceStock(id, request.quantity());
        return apiMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить товар", description = "Удаляет товар по ID и очищает его из кэша.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Товар успешно удалён"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID товара", example = "1") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
