package com.example.practice_2.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(

        @NotBlank
        String name,

        @Positive
        int pageSize,

        @NotNull @Valid
        Feature feature
) {
    public record Feature(
            @NotBlank String mode,
            @Positive int timeoutSeconds
    ) {}
}