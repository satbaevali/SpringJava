package com.example.sis1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Типобезопасная привязка параметров из application-*.properties с префиксом "app.*"
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String name,
        int pageSize,
        Feature feature
) {
    public record Feature(boolean smsEnabled) {
    }
}