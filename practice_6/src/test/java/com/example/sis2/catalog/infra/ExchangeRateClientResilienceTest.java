package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.ExchangeRateQuote;
import com.example.sis2.common.config.ExchangeClientProperties;
import com.example.sis2.common.config.RestClientConfig;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

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
    @DisplayName("Practice 6 Success Path: RestClient успешно получает актуальный курс от внешнего сервиса")
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

        ExchangeRateClient client = createClient("http://localhost:" + port, Duration.ofSeconds(1), Duration.ofSeconds(1));

        ExchangeRateQuote quote = client.fetchQuote("kzt");

        assertThat(quote.currency()).isEqualTo("KZT");
        assertThat(quote.rate()).isEqualByComparingTo("495.50");
        assertThat(quote.fallbackUsed()).isFalse();
        assertThat(quote.source()).isEqualTo("LIVE_REMOTE_SERVICE");
    }

    @Test
    @DisplayName("Practice 6 Failure Path (Service Stopped): когда внешний сервис остановлен, срабатывает Fallback")
    void fetchQuoteReturnsFallbackWhenRemoteServiceIsStopped() throws IOException {
        mockRemoteServer = HttpServer.create(new InetSocketAddress(0), 0);
        int closedPort = mockRemoteServer.getAddress().getPort();
        mockRemoteServer.stop(0);
        mockRemoteServer = null;

        ExchangeRateClient client = createClient("http://localhost:" + closedPort, Duration.ofMillis(300), Duration.ofMillis(300));

        ExchangeRateQuote quote = client.fetchQuote("KZT");

        assertThat(quote.currency()).isEqualTo("KZT");
        assertThat(quote.rate()).isEqualByComparingTo("500.00");
        assertThat(quote.fallbackUsed()).isTrue();
        assertThat(quote.source()).isEqualTo("FALLBACK_CONFIG");
    }

    @Test
    @DisplayName("Practice 6 Failure Path (Read Timeout): при превышении read-timeout срабатывает Fallback")
    void fetchQuoteReturnsFallbackOnReadTimeout() throws IOException {
        mockRemoteServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockRemoteServer.createContext("/api/rates/EUR", exchange -> {
            try {
                Thread.sleep(500);
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

        ExchangeRateClient client = createClient("http://localhost:" + port, Duration.ofMillis(200), Duration.ofMillis(100));

        ExchangeRateQuote quote = client.fetchQuote("EUR");

        assertThat(quote.currency()).isEqualTo("EUR");
        assertThat(quote.rate()).isEqualByComparingTo("0.92");
        assertThat(quote.fallbackUsed()).isTrue();
        assertThat(quote.source()).isEqualTo("FALLBACK_CONFIG");
    }

    @Test
    @DisplayName("Practice 6 Failure Path (HTTP 503): при ошибке 503 от внешнего сервиса срабатывает Fallback")
    void fetchQuoteReturnsFallbackOnHttp503Error() throws IOException {
        mockRemoteServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockRemoteServer.createContext("/api/rates/KZT", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        mockRemoteServer.start();
        int port = mockRemoteServer.getAddress().getPort();

        ExchangeRateClient client = createClient("http://localhost:" + port, Duration.ofSeconds(1), Duration.ofSeconds(1));

        ExchangeRateQuote quote = client.fetchQuote("KZT");

        assertThat(quote.currency()).isEqualTo("KZT");
        assertThat(quote.rate()).isEqualByComparingTo("500.00");
        assertThat(quote.fallbackUsed()).isTrue();
        assertThat(quote.source()).isEqualTo("FALLBACK_CONFIG");
    }

    private ExchangeRateClient createClient(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        ExchangeClientProperties props = new ExchangeClientProperties(
                baseUrl,
                connectTimeout,
                readTimeout,
                Map.of(
                        "KZT", new BigDecimal("500.00"),
                        "EUR", new BigDecimal("0.92"),
                        "USD", new BigDecimal("1.00")
                )
        );
        RestClient restClient = new RestClientConfig().exchangeRateRestClient(props);
        return new ExchangeRateClient(restClient, props);
    }
}
