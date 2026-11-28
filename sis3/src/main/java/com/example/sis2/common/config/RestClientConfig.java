package com.example.sis2.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(ExchangeClientProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient exchangeRateRestClient(ExchangeClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout() != null ? properties.connectTimeout() : Duration.ofSeconds(1));
        requestFactory.setReadTimeout(properties.readTimeout() != null ? properties.readTimeout() : Duration.ofSeconds(2));

        return RestClient.builder()
                .baseUrl(properties.baseUrl() != null ? properties.baseUrl() : "http://localhost:8090")
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public Retry exchangeRateRetry(ExchangeClientProperties properties) {
        int maxAttempts = properties.maxRetryAttempts() != null ? properties.maxRetryAttempts() : 3;
        Duration waitDuration = properties.retryWaitDuration() != null ? properties.retryWaitDuration() : Duration.ofMillis(100);

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .retryExceptions(RestClientException.class)
                .build();

        return RetryRegistry.of(config).retry("exchangeRateRetry");
    }

    @Bean
    public CircuitBreaker exchangeRateCircuitBreaker(ExchangeClientProperties properties) {
        float failureThreshold = properties.circuitBreakerFailureRateThreshold() != null
                ? properties.circuitBreakerFailureRateThreshold()
                : 50.0f;
        int minCalls = properties.circuitBreakerMinimumCalls() != null
                ? properties.circuitBreakerMinimumCalls()
                : 4;
        Duration waitInOpen = properties.circuitBreakerWaitDurationInOpenState() != null
                ? properties.circuitBreakerWaitDurationInOpenState()
                : Duration.ofSeconds(5);

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureThreshold)
                .minimumNumberOfCalls(minCalls)
                .slidingWindowSize(minCalls)
                .waitDurationInOpenState(waitInOpen)
                .permittedNumberOfCallsInHalfOpenState(2)
                .recordExceptions(RestClientException.class)
                .build();

        return CircuitBreakerRegistry.of(config).circuitBreaker("exchangeRateCircuitBreaker");
    }
}
