package com.alexstudio.ai.match.dto;
import java.util.List;
public record PlayingStyleRadarResponse(String status, int sampleLimit, String referenceMethod, RadarTeam home,
        RadarTeam away) {
    public record RadarTeam(Long teamId, String teamName, int historyMatches, List<RadarAxis> axes) {
    }
    public record RadarAxis(String key, String label, Double score, RadarMetric primary, List<RadarMetric> supporting,
            int referenceProfiles) {
    }
    public record RadarMetric(String name, Double average, int availableMatches, String unit) {
    }
}
