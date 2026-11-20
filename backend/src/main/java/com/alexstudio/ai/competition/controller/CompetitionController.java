package com.alexstudio.ai.competition.controller;

import com.alexstudio.ai.competition.dto.CompetitionRequest;
import com.alexstudio.ai.competition.dto.CompetitionResponse;
import com.alexstudio.ai.competition.entity.CompetitionType;
import com.alexstudio.ai.competition.service.CompetitionService;

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
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CompetitionService service;

    public CompetitionController(CompetitionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionResponse create(@Valid @RequestBody CompetitionRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public CompetitionResponse get(@PathVariable @Min(1) Long id) {
        return service.get(id);
    }

    @GetMapping
    public PagedModel<CompetitionResponse> list(@RequestParam(required = false) String name,
            @RequestParam(required = false) CompetitionType type, @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new PagedModel<>(service.list(name, type, page, size));
    }

    @PutMapping("/{id}")
    public CompetitionResponse update(@PathVariable @Min(1) Long id, @Valid @RequestBody CompetitionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
    }
}
