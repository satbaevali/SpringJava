package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.ExchangeRateQuote;
import com.example.sis2.common.config.ExchangeClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

@Component
public class ExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateClient.class);
    private static final BigDecimal DEFAULT_FALLBACK_RATE = new BigDecimal("500.00");

    private final RestClient restClient;
    private final ExchangeClientProperties properties;

    public ExchangeRateClient(RestClient exchangeRateRestClient, ExchangeClientProperties properties) {
        this.restClient = exchangeRateRestClient;
        this.properties = properties;
    }

    public ExchangeRateQuote fetchQuote(String currency) {
        String normalizedCurrency = (currency == null || currency.isBlank())
                ? "KZT"
                : currency.trim().toUpperCase(Locale.ROOT);

        try {
            ExternalRatePayload response = restClient.get()
                    .uri("/api/rates/{currency}", normalizedCurrency)
                    .retrieve()
                    .body(ExternalRatePayload.class);

            if (response != null && response.rate() != null && response.rate().compareTo(BigDecimal.ZERO) > 0) {
                return new ExchangeRateQuote(normalizedCurrency, response.rate(), false, "LIVE_REMOTE_SERVICE");
            }
        } catch (RestClientException ex) {
            log.warn("Внешний сервис курсов валют недоступен для {}: {}. Используется fallback-курс.",
                    normalizedCurrency, ex.getMessage());
        }

        return buildFallbackQuote(normalizedCurrency);
    }

    private ExchangeRateQuote buildFallbackQuote(String normalizedCurrency) {
        Map<String, BigDecimal> configuredFallbacks = properties.fallbackRates();
        BigDecimal fallbackRate = (configuredFallbacks != null)
                ? configuredFallbacks.getOrDefault(normalizedCurrency, DEFAULT_FALLBACK_RATE)
                : DEFAULT_FALLBACK_RATE;
        return new ExchangeRateQuote(normalizedCurrency, fallbackRate, true, "FALLBACK_CONFIG");
    }

    public record ExternalRatePayload(String currency, BigDecimal rate) {
    }
}
