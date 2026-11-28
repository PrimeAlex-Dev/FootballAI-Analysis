package com.alexstudio.ai.standing.controller;
import com.alexstudio.ai.standing.dto.*;
import com.alexstudio.ai.standing.service.StandingService;
import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class StandingController {
    private final StandingService service;
    public StandingController(StandingService service) {
        this.service = service;
    }
    @GetMapping("/standings")
    public PagedModel<StandingSummary> list(@RequestParam(required = false) @Positive Long competitionSeasonId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new PagedModel<>(service.list(competitionSeasonId, page, size));
    }
    @GetMapping("/standings/{id}")
    public StandingResponse get(@PathVariable @Positive Long id) {
        return service.get(id);
    }
    @GetMapping("/competition-seasons/{id}/standing")
    public StandingResponse getByCompetitionSeason(@PathVariable @Positive Long id) {
        return service.getByCompetitionSeason(id);
    }
    @PostMapping("/competition-seasons/{id}/standing/preview")
    public StandingPreview preview(@PathVariable @Positive Long id, @RequestBody JsonNode input) {
        return service.preview(id, input);
    }
    @PostMapping("/competition-seasons/{id}/standing")
    @ResponseStatus(HttpStatus.CREATED)
    public StandingResponse create(@PathVariable @Positive Long id, @RequestBody JsonNode input) {
        return service.create(id, input);
    }
    @PutMapping("/competition-seasons/{id}/standing")
    public StandingResponse update(@PathVariable @Positive Long id, @RequestBody JsonNode input) {
        return service.update(id, input);
    }
    @DeleteMapping("/competition-seasons/{id}/standing")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }
}
