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
                        .title("Catalog & Inventory API")
                        .version("1.0.0")
                        .description("REST API каталога товаров и категорий с поддержкой кэширования, AOP-тайминга и отказоустойчивого внешнего RestClient")
                        .contact(new Contact().name("KBTU INFT3132")));
    }
}
