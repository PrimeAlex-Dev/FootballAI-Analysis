package com.alexstudio.ai.competitionseason.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompetitionSeasonRequest(@NotNull @Positive Long competitionId, @NotNull @Positive Long seasonId) {
}
