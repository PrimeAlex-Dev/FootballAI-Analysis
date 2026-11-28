package com.alexstudio.ai.standing.dto;
import java.time.Instant;
import java.util.List;
public record StandingResponse(Long id, Long competitionSeasonId, String competitionSeasonName, Instant createdAt,
        Instant updatedAt, List<List<StandingRowResponse>> tables, boolean hidden) {
}
