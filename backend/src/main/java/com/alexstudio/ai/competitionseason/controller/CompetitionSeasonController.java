package com.alexstudio.ai.competitionseason.controller;

import com.alexstudio.ai.competitionseason.dto.CompetitionSeasonRequest;
import com.alexstudio.ai.competitionseason.dto.CompetitionSeasonResponse;
import com.alexstudio.ai.competitionseason.service.CompetitionSeasonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/competition-seasons")
public class CompetitionSeasonController {
    private final CompetitionSeasonService service;
    public CompetitionSeasonController(CompetitionSeasonService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionSeasonResponse create(@Valid @RequestBody CompetitionSeasonRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public CompetitionSeasonResponse get(@PathVariable @Min(1) Long id) {
        return service.get(id);
    }

    @GetMapping
    public PagedModel<CompetitionSeasonResponse> list(@RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Long seasonId, @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new PagedModel<>(service.list(competitionId, seasonId, page, size));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
    }
}
