package com.alexstudio.ai.season.dto;

import com.alexstudio.ai.season.entity.Season;

public record SeasonResponse(Long id, Long competitionId, String name) {

    public static SeasonResponse from(Season season) {
        return new SeasonResponse(season.getId(), season.getCompetitionId(), season.getName());
    }
}
