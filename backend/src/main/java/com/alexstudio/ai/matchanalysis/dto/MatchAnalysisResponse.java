package com.alexstudio.ai.matchanalysis.dto;

import com.alexstudio.ai.matchanalysis.entity.AnalysisStatus;

public record MatchAnalysisResponse(Long matchId, String analysisContent, String predictionContent,
        AnalysisStatus status) {
}
