package com.example.sis2.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Catalog & Inventory API — SIS 3")
                        .version("1.0.0")
                        .description("SIS 3: OpenAPI Documentation, Resilient RestClient (Timeouts, Retry, Circuit Breaker, Fallback), AOP Timing, Bounded Caffeine Cache, and Kafka Domain Events with Idempotent Consumer & Dead-Letter Topic")
                        .contact(new Contact().name("KBTU INFT3132")));
    }
}
