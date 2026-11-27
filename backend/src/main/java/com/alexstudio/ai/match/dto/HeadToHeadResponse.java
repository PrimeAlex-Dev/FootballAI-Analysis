package com.alexstudio.ai.match.dto;

import org.springframework.data.web.PagedModel;

public record HeadToHeadResponse(Long homeTeamId, Long awayTeamId, long homeWins, long draws, long awayWins,
        PagedModel<MatchResponse> matches) {
}
