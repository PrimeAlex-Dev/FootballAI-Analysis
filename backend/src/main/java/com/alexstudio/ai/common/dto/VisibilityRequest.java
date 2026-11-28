package com.alexstudio.ai.common.dto;
import jakarta.validation.constraints.NotNull;
public record VisibilityRequest(@NotNull Boolean hidden) {
}
