package com.alexstudio.ai.match.dto;
public record MatchAnalysisPromptResponse(String prompt, int homeRecentCount, int awayRecentCount,
        int homeMatchesWithStatistics, int awayMatchesWithStatistics) {
}
