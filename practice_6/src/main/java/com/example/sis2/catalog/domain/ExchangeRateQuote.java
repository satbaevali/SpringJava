package com.example.sis2.catalog.domain;

import java.math.BigDecimal;

public record ExchangeRateQuote(
        String currency,
        BigDecimal rate,
        boolean fallbackUsed,
        String source
) {
}
