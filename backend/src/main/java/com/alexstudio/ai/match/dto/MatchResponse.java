package com.alexstudio.ai.match.dto;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alexstudio.ai.match.entity.MatchStatus;
public record MatchResponse(Long id, Long competitionSeasonId, Long homeTeamId, Long awayTeamId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX", timezone = "Asia/Ho_Chi_Minh") Instant kickoffTime,
        MatchStatus status, Integer homeScore, Integer awayScore, String round, String competitionSeasonName,
        String homeTeamName, String homeTeamLogo, String awayTeamName, String awayTeamLogo, Boolean hasStatistics,
        boolean hidden) {
}
