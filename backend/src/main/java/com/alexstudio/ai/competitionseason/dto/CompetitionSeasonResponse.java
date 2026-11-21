package com.alexstudio.ai.competitionseason.dto;

import com.alexstudio.ai.competitionseason.entity.CompetitionSeason;

public record CompetitionSeasonResponse(Long id, Long competitionId, Long seasonId, String displayName) {
    public static CompetitionSeasonResponse from(CompetitionSeason value) {
        return new CompetitionSeasonResponse(value.getId(), value.getCompetition().getId(), value.getSeason().getId(),
                value.getCompetition().getName() + " " + value.getSeason().getName());
    }
}
