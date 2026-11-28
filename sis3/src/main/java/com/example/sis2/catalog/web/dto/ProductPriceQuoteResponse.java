package com.example.sis2.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ответ с расчётом цены товара в выбранной валюте (с поддержкой fallback)")
public record ProductPriceQuoteResponse(
        @Schema(description = "ID товара", example = "1")
        Long productId,

        @Schema(description = "Название товара", example = "MacBook Pro 16")
        String productName,

        @Schema(description = "Базовая цена в USD", example = "2499.99")
        BigDecimal basePriceUsd,

        @Schema(description = "Целевая валюта", example = "KZT")
        String targetCurrency,

        @Schema(description = "Применённый курс обмена", example = "500.00")
        BigDecimal exchangeRate,

        @Schema(description = "Итоговая цена в целевой валюте", example = "1249995.00")
        BigDecimal convertedPrice,

        @Schema(description = "Был ли использован резервный (fallback) курс из-за недоступности внешнего сервиса", example = "true")
        boolean fallbackUsed,

        @Schema(description = "Источник курса валют", example = "FALLBACK_CONFIG")
        String rateSource
) {
}
