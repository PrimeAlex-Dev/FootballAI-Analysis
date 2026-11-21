package com.alexstudio.ai.match.dto;
import java.time.Instant;
import com.alexstudio.ai.match.entity.MatchStatus;
import jakarta.validation.constraints.*;
import tools.jackson.databind.annotation.JsonDeserialize;
public record MatchRequest(@NotNull @Positive Long competitionSeasonId, @NotNull @Positive Long homeTeamId,
        @NotNull @Positive Long awayTeamId, @NotNull Instant kickoffTime, MatchStatus status,
        @PositiveOrZero @JsonDeserialize(using = ScoreDeserializer.class) Integer homeScore,
        @PositiveOrZero @JsonDeserialize(using = ScoreDeserializer.class) Integer awayScore, String round) {
}
