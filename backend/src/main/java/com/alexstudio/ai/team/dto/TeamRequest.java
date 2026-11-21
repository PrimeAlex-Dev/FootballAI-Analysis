package com.alexstudio.ai.team.dto;

import com.alexstudio.ai.team.entity.TeamType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamRequest(@NotBlank String name, @NotNull TeamType type, String logo) {
}
