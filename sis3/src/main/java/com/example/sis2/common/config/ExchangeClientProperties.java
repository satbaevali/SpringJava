package com.example.sis2.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "app.exchange")
public record ExchangeClientProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        Integer maxRetryAttempts,
        Duration retryWaitDuration,
        Float circuitBreakerFailureRateThreshold,
        Integer circuitBreakerMinimumCalls,
        Duration circuitBreakerWaitDurationInOpenState,
        Map<String, BigDecimal> fallbackRates
) {
}
