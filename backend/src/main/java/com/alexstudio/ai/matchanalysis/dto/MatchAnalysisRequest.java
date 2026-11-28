package com.alexstudio.ai.matchanalysis.dto;

import com.alexstudio.ai.matchanalysis.entity.AnalysisStatus;
import jakarta.validation.constraints.NotNull;

public record MatchAnalysisRequest(String analysisContent, String predictionContent, @NotNull AnalysisStatus status) {
}
