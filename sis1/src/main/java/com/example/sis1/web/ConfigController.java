package com.example.sis1.web;

import com.example.sis1.config.AppProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    private final AppProperties appProperties;
    private final Environment environment;

    public ConfigController(AppProperties appProperties, Environment environment) {
        this.appProperties = appProperties;
        this.environment = environment;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        boolean smsEnabled = appProperties.feature() != null && appProperties.feature().smsEnabled();
        return ResponseEntity.ok(Map.of(
                "activeProfiles", Arrays.asList(environment.getActiveProfiles()),
                "appName", appProperties.name(),
                "pageSize", appProperties.pageSize(),
                "smsEnabled", smsEnabled
        ));
    }
}