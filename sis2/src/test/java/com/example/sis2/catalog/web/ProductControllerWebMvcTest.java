package com.example.sis2.catalog.web;

import com.example.sis2.catalog.app.ProductService;
import com.example.sis2.catalog.domain.Money;
import com.example.sis2.catalog.domain.Product;
import com.example.sis2.common.exception.GlobalExceptionHandler;
import com.example.sis2.common.exception.InsufficientStockException;
import com.example.sis2.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({ProductApiMapper.class, GlobalExceptionHandler.class})
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products возвращает 200 OK и страницу ProductResponse DTO")
    void getProductsReturnsPagedDtos() throws Exception {
        Product sample = new Product(1L, "MacBook Air", Money.of("1199.99"), 10, 1L, "Электроника", 0L);
        when(productService.getProducts(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample)));

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("MacBook Air"))
                .andExpect(jsonPath("$.content[0].price").value(1199.99))
                .andExpect(jsonPath("$.content[0].categoryName").value("Электроника"));
    }

    @Test
    @DisplayName("POST /api/products с валидным DTO возвращает 201 Created и заголовок Location")
    void createProductReturns201AndLocation() throws Exception {
        Product created = new Product(42L, "iPhone 16", Money.of("999.99"), 20, 1L, "Электроника", 0L);
        when(productService.createProduct(eq("iPhone 16"), any(BigDecimal.class), eq(20), eq(1L)))
                .thenReturn(created);

        String jsonBody = """
                {
                  "name": "iPhone 16",
                  "price": 999.99,
                  "stock": 20,
                  "categoryId": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("iPhone 16"));
    }

    @Test
    @DisplayName("POST /api/products с пустым именем и отрицательной ценой возвращает 400 Bad Request (ProblemDetail)")
    void createProductInvalidInputReturns400ProblemDetail() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "price": -50,
                  "stock": -1,
                  "categoryId": null
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ошибка валидации"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    @DisplayName("GET /api/products/999 возвращает 404 Not Found (ProblemDetail)")
    void getProductNotFoundReturns404() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Товар с ID 999 не найден"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Товар с ID 999 не найден"));
    }

    @Test
    @DisplayName("PATCH /api/products/1/reduce-stock при нехватке остатка возвращает 409 Conflict")
    void reduceStockInsufficientReturns409() throws Exception {
        when(productService.reduceStock(1L, 500))
                .thenThrow(new InsufficientStockException("Недостаточно товара на складе!"));

        mockMvc.perform(patch("/api/products/1/reduce-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 500}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
