package com.alexstudio.ai.competition.dto;

import com.alexstudio.ai.competition.entity.Competition;
import com.alexstudio.ai.competition.entity.CompetitionType;

public record CompetitionResponse(Long id, String name, CompetitionType type, String logo) {

    public static CompetitionResponse from(Competition competition) {
        return new CompetitionResponse(competition.getId(), competition.getName(), competition.getType(),
                competition.getLogo());
    }
}
