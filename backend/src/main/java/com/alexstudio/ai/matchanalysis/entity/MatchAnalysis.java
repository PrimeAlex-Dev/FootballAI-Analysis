package com.alexstudio.ai.matchanalysis.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "match_analyses")
public class MatchAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long matchId;
    @Column(nullable = false, columnDefinition = "text")
    private String analysisContent;
    @Column(nullable = false, columnDefinition = "text")
    private String predictionContent;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status;

    protected MatchAnalysis() {
    }

    public MatchAnalysis(Long matchId, String analysisContent, String predictionContent, AnalysisStatus status) {
        this.matchId = matchId;
        update(analysisContent, predictionContent, status);
    }

    public void update(String analysisContent, String predictionContent, AnalysisStatus status) {
        this.analysisContent = analysisContent;
        this.predictionContent = predictionContent;
        this.status = status;
    }

    public Long getMatchId() {
        return matchId;
    }

    public String getAnalysisContent() {
        return analysisContent;
    }

    public String getPredictionContent() {
        return predictionContent;
    }

    public AnalysisStatus getStatus() {
        return status;
    }
}
