package com.alexstudio.ai.match.controller;
import java.time.Instant;
import com.alexstudio.ai.match.dto.*;
import com.alexstudio.ai.match.entity.MatchStatus;
import com.alexstudio.ai.match.service.MatchService;
import com.alexstudio.ai.match.service.PlayingStyleRadarService;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/public/matches")
public class PublicMatchController {
    private final MatchService service;
    private final PlayingStyleRadarService radar;
    public PublicMatchController(MatchService service, PlayingStyleRadarService radar) {
        this.service = service;
        this.radar = radar;
    }
    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable @Min(1) Long id) {
        return service.getPublic(id);
    }
    @GetMapping("/{id}/playing-style-radar")
    public PlayingStyleRadarResponse playingStyleRadar(@PathVariable @Min(1) Long id) {
        return radar.get(id);
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
        return new PagedModel<>(service.listPublic(name, competitionSeasonId, homeTeamId, awayTeamId, teamId, status,
                from, to, direction, page, size));
    }
}
