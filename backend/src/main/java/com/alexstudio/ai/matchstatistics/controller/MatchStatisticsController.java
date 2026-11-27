package com.alexstudio.ai.matchstatistics.controller;
import com.alexstudio.ai.matchstatistics.dto.StatisticsResponse;
import com.alexstudio.ai.matchstatistics.service.MatchStatisticsService;
import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/matches/{matchId}/statistics")
public class MatchStatisticsController {
    private final MatchStatisticsService service;
    public MatchStatisticsController(MatchStatisticsService service) {
        this.service = service;
    }
    @GetMapping
    public StatisticsResponse get(@PathVariable @Min(1) Long matchId) {
        return service.get(matchId);
    }
    @PostMapping("/preview")
    public StatisticsResponse preview(@PathVariable @Min(1) Long matchId, @RequestBody JsonNode input) {
        return service.preview(matchId, input);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatisticsResponse create(@PathVariable @Min(1) Long matchId, @RequestBody JsonNode input) {
        return service.create(matchId, input);
    }
    @PutMapping
    public StatisticsResponse update(@PathVariable @Min(1) Long matchId, @RequestBody JsonNode input) {
        return service.update(matchId, input);
    }
}
