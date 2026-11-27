package com.alexstudio.ai.match.controller;
import java.time.Instant;
import com.alexstudio.ai.match.dto.*;
import com.alexstudio.ai.match.entity.MatchStatus;
import com.alexstudio.ai.match.service.MatchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService service;
    public MatchController(MatchService service) {
        this.service = service;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse create(@Valid @RequestBody MatchRequest request) {
        return service.create(request);
    }
    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable @Min(1) Long id) {
        return service.get(id);
    }
    @GetMapping("/{id}/recent-form")
    public RecentFormResponse recentForm(@PathVariable @Min(1) Long id) {
        return service.recentForm(id);
    }
    @GetMapping("/{id}/head-to-head")
    public HeadToHeadResponse headToHead(@PathVariable @Min(1) Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {
        return service.headToHead(id, page, size);
    }
    @PutMapping("/{id}")
    public MatchResponse update(@PathVariable @Min(1) Long id, @Valid @RequestBody MatchRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
    }
    @GetMapping
    public PagedModel<MatchResponse> list(@RequestParam(required = false) String name,
            @RequestParam(required = false) @Positive Long competitionSeasonId,
            @RequestParam(required = false) @Positive Long homeTeamId,
            @RequestParam(required = false) @Positive Long awayTeamId,
            @RequestParam(required = false) @Positive Long teamId, @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new PagedModel<>(service.list(name, competitionSeasonId, homeTeamId, awayTeamId, teamId, status, from,
                to, direction, page, size));
    }
}
