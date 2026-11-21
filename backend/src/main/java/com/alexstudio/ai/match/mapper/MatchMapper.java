package com.alexstudio.ai.match.mapper;
import com.alexstudio.ai.match.entity.Match;
import com.alexstudio.ai.match.entity.MatchStatus;
import com.alexstudio.ai.match.dto.MatchRequest;
public final class MatchMapper {
    private MatchMapper() {
    }
    public static Match create(MatchRequest request, MatchStatus status, Integer home, Integer away) {
        return new Match(request.competitionSeasonId(), request.homeTeamId(), request.awayTeamId(),
                request.kickoffTime(), status, home, away, normalizeRound(request.round()));
    }
    public static void update(Match match, MatchRequest request, MatchStatus status, Integer home, Integer away) {
        match.update(request.competitionSeasonId(), request.homeTeamId(), request.awayTeamId(), request.kickoffTime(),
                status, home, away, normalizeRound(request.round()));
    }
    private static String normalizeRound(String round) {
        return round == null || round.isBlank() ? null : round.trim();
    }
}
