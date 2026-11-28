package com.alexstudio.ai.standing.dto;
import java.util.List;
public record StandingPreview(Long competitionSeasonId, String competitionSeasonName,
        List<List<StandingRowResponse>> tables) {
}
