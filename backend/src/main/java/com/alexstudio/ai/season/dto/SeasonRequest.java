package com.alexstudio.ai.season.dto;

import jakarta.validation.constraints.NotBlank;

public record SeasonRequest(@NotBlank String name) {
}
