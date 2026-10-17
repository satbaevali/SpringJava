package com.example.practice4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Название категории обязательно")
        @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
        String name,

        @Size(max = 500, message = "Описание не должно превышать 500 символов")
        String description
) {}
