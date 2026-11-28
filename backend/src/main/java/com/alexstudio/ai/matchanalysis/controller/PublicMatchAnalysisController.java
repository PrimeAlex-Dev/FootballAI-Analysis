package com.alexstudio.ai.matchanalysis.controller;

import com.alexstudio.ai.matchanalysis.dto.MatchAnalysisResponse;
import com.alexstudio.ai.matchanalysis.service.MatchAnalysisService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/matches/{matchId}/analysis")
public class PublicMatchAnalysisController {
    private final MatchAnalysisService service;

    public PublicMatchAnalysisController(MatchAnalysisService service) {
        this.service = service;
    }

    @GetMapping
    public MatchAnalysisResponse get(@PathVariable @Min(1) Long matchId) {
        return service.getPublic(matchId);
    }
}
