package com.example.sis2.catalog.web;

import com.example.sis2.catalog.app.CategoryService;
import com.example.sis2.catalog.domain.Category;
import com.example.sis2.catalog.web.dto.CategoryRequest;
import com.example.sis2.catalog.web.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/categories", produces = "application/json;charset=UTF-8")
@Tag(name = "Categories", description = "Управление категориями каталога")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryApiMapper apiMapper;

    public CategoryController(CategoryService categoryService, CategoryApiMapper apiMapper) {
        this.categoryService = categoryService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    @Operation(summary = "Получить все категории", description = "Возвращает полный список категорий (кэшируется через @Cacheable).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен")
    })
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(apiMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить категорию по ID", description = "Возвращает категорию по идентификатору.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Категория найдена",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Категория не найдена",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public CategoryResponse getCategoryById(
            @Parameter(description = "ID категории", example = "1") @PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return apiMapper.toResponse(category);
    }

    @PostMapping
    @Operation(summary = "Создать категорию", description = "Создаёт новую категорию и сбрасывает кэш категорий.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Категория успешно создана",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Категория с таким именем уже существует",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category created = categoryService.createCategory(request.name(), request.description());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(apiMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить категорию", description = "Обновляет название и описание категории по ID.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Категория успешно обновлена",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
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
                    responseCode = "409",
                    description = "Конфликт имени категории",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public CategoryResponse updateCategory(
            @Parameter(description = "ID категории", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category updated = categoryService.updateCategory(id, request.name(), request.description());
        return apiMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить категорию", description = "Удаляет категорию по ID и очищает кэш.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Категория успешно удалена"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Категория не найдена",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID категории", example = "1") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
