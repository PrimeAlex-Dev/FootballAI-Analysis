package com.alexstudio.ai.competition.dto;

import com.alexstudio.ai.competition.entity.CompetitionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompetitionRequest(@NotBlank String name, @NotNull CompetitionType type, String logo) {
}
