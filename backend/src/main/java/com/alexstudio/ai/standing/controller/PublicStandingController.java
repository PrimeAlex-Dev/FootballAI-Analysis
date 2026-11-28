package com.alexstudio.ai.standing.controller;
import com.alexstudio.ai.standing.dto.StandingResponse;
import com.alexstudio.ai.standing.service.StandingService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/public/competition-seasons")
public class PublicStandingController {
    private final StandingService service;
    public PublicStandingController(StandingService service) {
        this.service = service;
    }
    @GetMapping("/{id}/standing")
    public StandingResponse get(@PathVariable @Positive Long id) {
        return service.getPublicByCompetitionSeason(id);
    }
}
