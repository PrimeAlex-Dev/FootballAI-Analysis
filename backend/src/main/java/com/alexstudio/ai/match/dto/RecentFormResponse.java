package com.alexstudio.ai.match.dto;

import java.util.List;

public record RecentFormResponse(Long homeTeamId, Long awayTeamId, List<FormMatchResponse> home,
        List<FormMatchResponse> away) {
}
