package com.alexstudio.ai.season.controller;

import com.alexstudio.ai.season.dto.SeasonRequest;
import com.alexstudio.ai.season.dto.SeasonResponse;
import com.alexstudio.ai.season.service.SeasonService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/competitions/{competitionId}/seasons")
public class SeasonController {

    private final SeasonService service;

    public SeasonController(SeasonService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SeasonResponse create(@PathVariable @Min(1) Long competitionId, @Valid @RequestBody SeasonRequest request) {
        return service.create(competitionId, request);
    }

    @GetMapping("/{id}")
    public SeasonResponse get(@PathVariable @Min(1) Long competitionId, @PathVariable @Min(1) Long id) {
        return service.get(competitionId, id);
    }

    @GetMapping
    public PagedModel<SeasonResponse> list(@PathVariable @Min(1) Long competitionId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new PagedModel<>(service.list(competitionId, page, size));
    }

    @PutMapping("/{id}")
    public SeasonResponse update(@PathVariable @Min(1) Long competitionId, @PathVariable @Min(1) Long id,
            @Valid @RequestBody SeasonRequest request) {
        return service.update(competitionId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Min(1) Long competitionId, @PathVariable @Min(1) Long id) {
        service.delete(competitionId, id);
    }
}
