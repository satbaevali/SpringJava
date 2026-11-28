package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.ExchangeRateQuote;
import com.example.sis2.common.config.ExchangeClientProperties;
import com.example.sis2.common.config.RestClientConfig;
import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateClientResilienceTest {

    private HttpServer mockRemoteServer;

    @AfterEach
    void tearDown() {
        if (mockRemoteServer != null) {
            mockRemoteServer.stop(0);
        }
    }

    @Test
    void fetchQuoteReturnsLiveRateWhenRemoteServiceIsUp() throws IOException {
        mockRemoteServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockRemoteServer.createContext("/api/rates/KZT", exchange -> {
            byte[] body = "{\"currency\":\"KZT\",\"rate\":495.50}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        mockRemoteServer.start();
        int port = mockRemoteServer.getAddress().getPort();

        ExchangeRateClient client = createClient("http://localhost:" + port, Duration.ofSeconds(1), Duration.ofSeconds(1), 3, 4);

        ExchangeRateQuote quote = client.fetchQuote("kzt");

        assertThat(quote.currency()).isEqualTo("KZT");
        assertThat(quote.rate()).isEqualByComparingTo("495.50");
        assertThat(quote.fallbackUsed()).isFalse();
        assertThat(quote.source()).isEqualTo("LIVE_REMOTE_SERVICE");
        assertThat(client.getCircuitBreakerState()).isEqualTo("CLOSED");
    }

    @Test
    void fetchQuoteRecoversViaRetryAfterTransientFailures() throws IOException {
        AtomicInteger attempts = new AtomicInteger(0);
        mockRemoteServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockRemoteServer.createContext("/api/rates/KZT", exchange -> {
            int currentAttempt = attempts.incrementAndGet();
            if (currentAttempt < 3) {
                exchange.sendResponseHeaders(503, -1);
                exchange.close();
                return;
            }
            byte[] body = "{\"currency\":\"KZT\",\"rate\":498.00}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        mockRemoteServer.start();
        int port = mockRemoteServer.getAddress().getPort();

        ExchangeRateClient client = createClient("http://localhost:" + port, Duration.ofSeconds(1), Duration.ofSeconds(1), 3, 10);

        ExchangeRateQuote quote = client.fetchQuote("KZT");

        assertThat(attempts.get()).isEqualTo(3);
        assertThat(quote.rate()).isEqualByComparingTo("498.00");
        assertThat(quote.fallbackUsed()).isFalse();
        assertThat(quote.source()).isEqualTo("LIVE_REMOTE_SERVICE");
    }

    @Test
    void fetchQuoteOpensCircuitBreakerAndReturnsFallbackWhenRemoteServiceIsStopped() throws IOException {
        mockRemoteServer = HttpServer.create(new InetSocketAddress(0), 0);
        int closedPort = mockRemoteServer.getAddress().getPort();
        mockRemoteServer.stop(0);
        mockRemoteServer = null;

        ExchangeRateClient client = createClient("http://localhost:" + closedPort, Duration.ofMillis(150), Duration.ofMillis(150), 2, 2);

        ExchangeRateQuote firstQuote = client.fetchQuote("KZT");

        assertThat(firstQuote.rate()).isEqualByComparingTo("500.00");
        assertThat(firstQuote.fallbackUsed()).isTrue();
        assertThat(client.getCircuitBreakerState()).isEqualTo("OPEN");

        ExchangeRateQuote openBreakerQuote = client.fetchQuote("KZT");
        assertThat(openBreakerQuote.fallbackUsed()).isTrue();
        assertThat(openBreakerQuote.source()).isEqualTo("CIRCUIT_BREAKER_OPEN_FALLBACK");
    }

    @Test
    void fetchQuoteReturnsFallbackOnReadTimeout() throws IOException {
        mockRemoteServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockRemoteServer.createContext("/api/rates/EUR", exchange -> {
            try {
                Thread.sleep(350);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "{\"currency\":\"EUR\",\"rate\":0.89}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        mockRemoteServer.start();
        int port = mockRemoteServer.getAddress().getPort();

        ExchangeRateClient client = createClient("http://localhost:" + port, Duration.ofMillis(200), Duration.ofMillis(80), 1, 4);

        ExchangeRateQuote quote = client.fetchQuote("EUR");

        assertThat(quote.currency()).isEqualTo("EUR");
        assertThat(quote.rate()).isEqualByComparingTo("0.92");
        assertThat(quote.fallbackUsed()).isTrue();
    }

    private ExchangeRateClient createClient(
            String baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            int maxRetryAttempts,
            int circuitBreakerMinCalls) {
        ExchangeClientProperties props = new ExchangeClientProperties(
                baseUrl,
                connectTimeout,
                readTimeout,
                maxRetryAttempts,
                Duration.ofMillis(20),
                50.0f,
                circuitBreakerMinCalls,
                Duration.ofSeconds(5),
                Map.of(
                        "KZT", new BigDecimal("500.00"),
                        "EUR", new BigDecimal("0.92"),
                        "USD", new BigDecimal("1.00")
                )
        );
        RestClientConfig config = new RestClientConfig();
        RestClient restClient = config.exchangeRateRestClient(props);
        Retry retry = config.exchangeRateRetry(props);
        CircuitBreaker circuitBreaker = config.exchangeRateCircuitBreaker(props);
        return new ExchangeRateClient(restClient, props, retry, circuitBreaker);
    }
}
