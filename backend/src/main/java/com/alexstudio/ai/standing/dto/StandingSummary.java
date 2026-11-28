package com.alexstudio.ai.standing.dto;
import java.time.Instant;
public record StandingSummary(Long id, Long competitionSeasonId, String competitionSeasonName, long tableCount,
        long teamCount, Instant updatedAt) {
}
