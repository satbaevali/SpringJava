package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.ExchangeRateQuote;
import com.example.sis2.common.config.ExchangeClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class ExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateClient.class);
    private static final BigDecimal DEFAULT_FALLBACK_RATE = new BigDecimal("500.00");

    private final RestClient restClient;
    private final ExchangeClientProperties properties;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public ExchangeRateClient(
            RestClient exchangeRateRestClient,
            ExchangeClientProperties properties,
            Retry exchangeRateRetry,
            CircuitBreaker exchangeRateCircuitBreaker) {
        this.restClient = exchangeRateRestClient;
        this.properties = properties;
        this.retry = exchangeRateRetry;
        this.circuitBreaker = exchangeRateCircuitBreaker;
    }

    public ExchangeRateQuote fetchQuote(String currency) {
        String normalizedCurrency = (currency == null || currency.isBlank())
                ? "KZT"
                : currency.trim().toUpperCase(Locale.ROOT);

        Supplier<ExternalRatePayload> remoteCall = () -> restClient.get()
                .uri("/api/rates/{currency}", normalizedCurrency)
                .retrieve()
                .body(ExternalRatePayload.class);

        Supplier<ExternalRatePayload> resilientCall = Retry.decorateSupplier(
                retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, remoteCall)
        );

        try {
            ExternalRatePayload response = resilientCall.get();
            if (response != null && response.rate() != null && response.rate().compareTo(BigDecimal.ZERO) > 0) {
                return new ExchangeRateQuote(normalizedCurrency, response.rate(), false, "LIVE_REMOTE_SERVICE");
            }
        } catch (CallNotPermittedException ex) {
            log.warn("Circuit Breaker находится в состоянии OPEN для {}: мгновенный возврат Fallback.", normalizedCurrency);
            return buildFallbackQuote(normalizedCurrency, "CIRCUIT_BREAKER_OPEN_FALLBACK");
        } catch (Exception ex) {
            log.warn("Внешний сервис курсов валют недоступен после Retry для {}: {}. Используется fallback-курс.",
                    normalizedCurrency, ex.getMessage());
        }

        return buildFallbackQuote(normalizedCurrency, "FALLBACK_CONFIG");
    }

    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }

    public void resetCircuitBreaker() {
        circuitBreaker.reset();
    }

    private ExchangeRateQuote buildFallbackQuote(String normalizedCurrency, String source) {
        Map<String, BigDecimal> configuredFallbacks = properties.fallbackRates();
        BigDecimal fallbackRate = (configuredFallbacks != null)
                ? configuredFallbacks.getOrDefault(normalizedCurrency, DEFAULT_FALLBACK_RATE)
                : DEFAULT_FALLBACK_RATE;
        return new ExchangeRateQuote(normalizedCurrency, fallbackRate, true, source);
    }

    public record ExternalRatePayload(String currency, BigDecimal rate) {
    }
}
