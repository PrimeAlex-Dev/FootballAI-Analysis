package com.alexstudio.ai.match.service;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.List;
import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.competitionseason.service.CompetitionSeasonService;
import com.alexstudio.ai.team.service.TeamService;
import com.alexstudio.ai.match.dto.*;
import com.alexstudio.ai.match.entity.*;
import com.alexstudio.ai.match.mapper.MatchMapper;
import com.alexstudio.ai.match.repository.MatchRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.web.PagedModel;
import org.springframework.transaction.annotation.Transactional;
@Service
public class MatchService {
    private final MatchRepository matches;
    private final TeamService teams;
    private final CompetitionSeasonService competitionSeasons;
    private final Clock clock;
    public MatchService(MatchRepository matches, TeamService teams, CompetitionSeasonService competitionSeasons,
            Clock clock) {
        this.matches = matches;
        this.teams = teams;
        this.competitionSeasons = competitionSeasons;
        this.clock = clock;
    }
    @Transactional
    public MatchResponse create(MatchRequest request) {
        MatchStatus status = validate(request);
        boolean scored = status == MatchStatus.LIVE || status == MatchStatus.FINISHED;
        Match match = MatchMapper.create(request, effectiveStatus(status, request.kickoffTime()),
                scored ? request.homeScore() : null, scored ? request.awayScore() : null);
        matches.saveAndFlush(match);
        return get(match.getId());
    }
    @Transactional
    public MatchResponse update(Long id, MatchRequest request) {
        Match match = matches.findForUpdate(id).orElseThrow(() -> missing());
        if (request.status() == null)
            throw invalid("Match status is required when updating");
        MatchStatus status = validate(request);
        boolean scored = status == MatchStatus.LIVE || status == MatchStatus.FINISHED;
        MatchMapper.update(match, request, effectiveStatus(status, request.kickoffTime()),
                scored ? request.homeScore() : null, scored ? request.awayScore() : null);
        matches.flush();
        return get(id);
    }
    @Transactional(readOnly = true)
    public MatchResponse get(Long id) {
        return matches.findResponse(id, false).orElseThrow(() -> missing());
    }
    @Transactional(readOnly = true)
    public MatchResponse getPublic(Long id) {
        return matches.findResponse(id, true).orElseThrow(() -> missing());
    }
    @Transactional
    public MatchResponse setHidden(Long id, boolean hidden) {
        Match match = matches.findForUpdate(id).orElseThrow(() -> missing());
        match.setHidden(hidden);
        matches.flush();
        return get(id);
    }
    @Transactional
    public void delete(Long id) {
        Match match = matches.findForUpdate(id).orElseThrow(() -> missing());
        matches.delete(match);
        matches.flush();
    }
    @Transactional(readOnly = true)
    public Page<MatchResponse> list(String name, Long competitionSeasonId, Long homeTeamId, Long awayTeamId,
            Long teamId, MatchStatus status, Instant from, Instant to, Sort.Direction direction, int page, int size) {
        return listFiltered(name, competitionSeasonId, homeTeamId, awayTeamId, teamId, status, from, to, direction,
                null, page, size);
    }
    @Transactional(readOnly = true)
    public Page<MatchResponse> listFiltered(String name, Long competitionSeasonId, Long homeTeamId, Long awayTeamId,
            Long teamId, MatchStatus status, Instant from, Instant to, Sort.Direction direction, Boolean hidden,
            int page, int size) {
        if (from != null && to != null && from.isAfter(to))
            throw invalid("Start time must not be after end time");
        String search = name == null || name.isBlank()
                ? null
                : "%" + name.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_",
                        "\\_") + "%";
        return matches.search(search, competitionSeasonId, homeTeamId, awayTeamId, teamId, status, from, to, false,
                hidden, PageRequest.of(page, size, Sort.by(direction, "kickoffTime", "id")));
    }
    @Transactional(readOnly = true)
    public Page<MatchResponse> listPublic(String name, Long competitionSeasonId, Long homeTeamId, Long awayTeamId,
            Long teamId, MatchStatus status, Instant from, Instant to, Sort.Direction direction, int page, int size) {
        if (from != null && to != null && from.isAfter(to))
            throw invalid("Start time must not be after end time");
        String search = name == null || name.isBlank()
                ? null
                : "%" + name.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_",
                        "\\_") + "%";
        return matches.search(search, competitionSeasonId, homeTeamId, awayTeamId, teamId, status, from, to, true, null,
                PageRequest.of(page, size, Sort.by(direction, "kickoffTime", "id")));
    }

    @Transactional(readOnly = true)
    public RecentFormResponse recentForm(Long id) {
        Match match = matches.findById(id).filter(value -> !value.isHidden()).orElseThrow(() -> missing());
        List<FormMatchResponse> home = formFor(match.getHomeTeamId(), match.getKickoffTime());
        List<FormMatchResponse> away = formFor(match.getAwayTeamId(), match.getKickoffTime());
        return new RecentFormResponse(match.getHomeTeamId(), match.getAwayTeamId(), home, away);
    }

    private List<FormMatchResponse> formFor(Long teamId, Instant beforeTime) {
        return matches.recentForm(teamId, beforeTime,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "kickoffTime", "id"))).stream().map(item -> {
                    int own = item.homeTeamId().equals(teamId) ? item.homeScore() : item.awayScore();
                    int other = item.homeTeamId().equals(teamId) ? item.awayScore() : item.homeScore();
                    String outcome = own > other ? "W" : own < other ? "L" : "D";
                    return new FormMatchResponse(item, outcome);
                }).toList();
    }

    @Transactional(readOnly = true)
    public HeadToHeadResponse headToHead(Long id, int page, int size) {
        Match match = matches.findById(id).filter(value -> !value.isHidden()).orElseThrow(() -> missing());
        Long homeId = match.getHomeTeamId();
        Long awayId = match.getAwayTeamId();
        Instant beforeTime = match.getKickoffTime();
        Page<MatchResponse> history = matches.headToHead(homeId, awayId, beforeTime,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "kickoffTime", "id")));
        long homeWins = matches.countHomeWins(homeId, awayId, beforeTime);
        long draws = matches.countDraws(homeId, awayId, beforeTime);
        return new HeadToHeadResponse(homeId, awayId, homeWins, draws, history.getTotalElements() - homeWins - draws,
                new PagedModel<>(history));
    }
    @Transactional
    public int promoteDue() {
        return matches.promoteDue(clock.instant());
    }
    private MatchStatus validate(MatchRequest request) {
        competitionSeasons.get(request.competitionSeasonId());
        teams.get(request.homeTeamId());
        teams.get(request.awayTeamId());
        if (request.homeTeamId().equals(request.awayTeamId()))
            throw invalid("Home and away teams must be different");
        if ((request.homeScore() == null) != (request.awayScore() == null))
            throw invalid("Both scores must be provided together");
        MatchStatus status = request.status() == null ? MatchStatus.SCHEDULED : request.status();
        if (status == MatchStatus.FINISHED && request.homeScore() == null)
            throw invalid("Finished match requires both scores");
        // Normalize scores according to the requested status before automatic
        // promotion.
        return status;
    }
    private MatchStatus effectiveStatus(MatchStatus status, Instant kickoff) {
        return status == MatchStatus.SCHEDULED && !kickoff.isAfter(clock.instant()) ? MatchStatus.LIVE : status;
    }
    private BusinessException invalid(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, message);
    }
    private BusinessException missing() {
        return new BusinessException(HttpStatus.NOT_FOUND, "Match not found");
    }
}
