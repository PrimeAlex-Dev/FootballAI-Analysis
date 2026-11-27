package com.alexstudio.ai.matchstatistics.dto;
import java.time.Instant;
import com.alexstudio.ai.matchstatistics.entity.StatisticsSide;
public record StatisticsResponse(Long matchId, StatisticsSide home, StatisticsSide away, Instant createdAt,
        Instant updatedAt) {
}
