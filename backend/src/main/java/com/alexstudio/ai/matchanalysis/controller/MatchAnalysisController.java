package com.alexstudio.ai.matchanalysis.controller;

import com.alexstudio.ai.matchanalysis.dto.*;
import com.alexstudio.ai.matchanalysis.service.MatchAnalysisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches/{matchId}/analysis")
public class MatchAnalysisController {
    private final MatchAnalysisService service;

    public MatchAnalysisController(MatchAnalysisService service) {
        this.service = service;
    }

    @GetMapping
    public MatchAnalysisResponse get(@PathVariable @Min(1) Long matchId) {
        return service.get(matchId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchAnalysisResponse create(@PathVariable @Min(1) Long matchId,
            @Valid @RequestBody MatchAnalysisRequest request) {
        return service.create(matchId, request);
    }

    @PutMapping
    public MatchAnalysisResponse update(@PathVariable @Min(1) Long matchId,
            @Valid @RequestBody MatchAnalysisRequest request) {
        return service.update(matchId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Min(1) Long matchId) {
        service.delete(matchId);
    }
}
