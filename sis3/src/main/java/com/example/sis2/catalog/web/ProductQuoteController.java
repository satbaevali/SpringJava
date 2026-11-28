package com.example.sis2.catalog.web;

import com.example.sis2.catalog.app.ProductService;
import com.example.sis2.catalog.domain.ExchangeRateQuote;
import com.example.sis2.catalog.domain.Product;
import com.example.sis2.catalog.infra.ExchangeRateClient;
import com.example.sis2.catalog.web.dto.ProductPriceQuoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping(value = "/api/products", produces = "application/json;charset=UTF-8")
@Tag(name = "Products", description = "Управление товарами и расчёт стоимости в валютах")
public class ProductQuoteController {

    private final ProductService productService;
    private final ExchangeRateClient exchangeRateClient;

    public ProductQuoteController(ProductService productService, ExchangeRateClient exchangeRateClient) {
        this.productService = productService;
        this.exchangeRateClient = exchangeRateClient;
    }

    @GetMapping("/{id}/quote")
    @Operation(
            summary = "Рассчитать цену товара в другой валюте (внешний RestClient + Fallback)",
            description = "Обращается к внешнему сервису курсов валют с настроенными connect/read таймаутами. Если внешний сервис остановлен или недоступен, автоматически возвращает расчёт по резервному (fallback) курсу."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный расчёт стоимости товара (из внешнего сервиса или fallback)",
                    content = @Content(schema = @Schema(implementation = ProductPriceQuoteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ProductPriceQuoteResponse getProductPriceQuote(
            @Parameter(description = "ID товара", example = "1") @PathVariable Long id,
            @Parameter(description = "Код целевой валюты (KZT, EUR, USD)", example = "KZT")
            @RequestParam(defaultValue = "KZT") String currency) {

        Product product = productService.getProductById(id);
        ExchangeRateQuote quote = exchangeRateClient.fetchQuote(currency);
        BigDecimal basePrice = product.getPrice().amount();
        BigDecimal convertedPrice = basePrice.multiply(quote.rate()).setScale(2, RoundingMode.HALF_UP);

        return new ProductPriceQuoteResponse(
                product.getId(),
                product.getName(),
                basePrice,
                quote.currency(),
                quote.rate(),
                convertedPrice,
                quote.fallbackUsed(),
                quote.source()
        );
    }
}
