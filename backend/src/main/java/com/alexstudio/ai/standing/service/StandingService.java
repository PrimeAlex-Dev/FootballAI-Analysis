package com.alexstudio.ai.standing.service;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.competitionseason.service.CompetitionSeasonService;
import com.alexstudio.ai.standing.dto.*;
import com.alexstudio.ai.standing.entity.*;
import com.alexstudio.ai.standing.repository.*;
import com.alexstudio.ai.team.dto.TeamResponse;
import com.alexstudio.ai.team.service.TeamService;
import tools.jackson.databind.JsonNode;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class StandingService {
    private static final Set<String> ROW_FIELDS = Set.of("rank", "team", "matchesPlayed", "wins", "draws", "losses",
            "goalsFor", "goalsAgainst", "goalDifference", "points");
    private final StandingRepository standings;
    private final StandingRowRepository rows;
    private final CompetitionSeasonService competitionSeasons;
    private final TeamService teams;
    private final Clock clock;
    public StandingService(StandingRepository standings, StandingRowRepository rows,
            CompetitionSeasonService competitionSeasons, TeamService teams, Clock clock) {
        this.standings = standings;
        this.rows = rows;
        this.competitionSeasons = competitionSeasons;
        this.teams = teams;
        this.clock = clock;
    }
    @Transactional(readOnly = true)
    public Page<StandingSummary> list(Long competitionSeasonId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Standing> result = competitionSeasonId == null
                ? standings.findAll(pageable)
                : standings.findPageByCompetitionSeasonId(competitionSeasonId, pageable);
        return result.map(value -> {
            List<StandingRow> items = rows.findByStandingIdOrderByTableIndexAscRowIndexAsc(value.getId());
            return new StandingSummary(value.getId(), value.getCompetitionSeasonId(),
                    competitionSeasons.get(value.getCompetitionSeasonId()).displayName(),
                    items.stream().map(StandingRow::getTableIndex).distinct().count(), items.size(),
                    value.getUpdatedAt());
        });
    }
    @Transactional(readOnly = true)
    public StandingResponse get(Long id) {
        return response(standings.findById(id).orElseThrow(this::missing));
    }
    @Transactional(readOnly = true)
    public StandingResponse getByCompetitionSeason(Long id) {
        competitionSeasons.get(id);
        return response(standings.findByCompetitionSeasonId(id).orElseThrow(this::missing));
    }
    @Transactional(readOnly = true)
    public StandingPreview preview(Long id, JsonNode input) {
        var link = competitionSeasons.get(id);
        return new StandingPreview(id, link.displayName(), parse(input, false));
    }
    @Transactional
    public StandingResponse create(Long id, JsonNode input) {
        competitionSeasons.get(id);
        List<List<StandingRowResponse>> parsed = parse(input, true);
        if (standings.existsByCompetitionSeasonId(id))
            throw conflict();
        Standing standing = standings.saveAndFlush(new Standing(id, clock.instant().truncatedTo(ChronoUnit.MICROS)));
        saveRows(standing.getId(), parsed);
        return response(standing);
    }
    @Transactional
    public StandingResponse update(Long id, JsonNode input) {
        competitionSeasons.get(id);
        List<List<StandingRowResponse>> parsed = parse(input, true);
        Standing standing = standings.lockByCompetitionSeasonId(id).orElseThrow(this::missing);
        rows.deleteByStandingId(standing.getId());
        rows.flush();
        saveRows(standing.getId(), parsed);
        standing.touch(clock.instant().truncatedTo(ChronoUnit.MICROS));
        standings.flush();
        return response(standing);
    }
    @Transactional
    public void delete(Long id) {
        competitionSeasons.get(id);
        Standing standing = standings.lockByCompetitionSeasonId(id).orElseThrow(this::missing);
        rows.deleteByStandingId(standing.getId());
        rows.flush();
        standings.delete(standing);
        standings.flush();
    }
    private void saveRows(Long id, List<List<StandingRowResponse>> tables) {
        List<StandingRow> values = new ArrayList<>();
        for (var table : tables)
            for (var row : table)
                values.add(new StandingRow(id, row.tableIndex(), row.rowIndex(), row.teamId(), row.rank(),
                        row.matchesPlayed(), row.wins(), row.draws(), row.losses(), row.goalsFor(), row.goalsAgainst(),
                        row.goalDifference(), row.points()));
        rows.saveAllAndFlush(values);
    }
    private StandingResponse response(Standing standing) {
        List<List<StandingRowResponse>> tables = new ArrayList<>();
        List<StandingRow> standingRows = rows.findByStandingIdOrderByTableIndexAscRowIndexAsc(standing.getId());
        Map<Long, TeamResponse> teamById = teams.getByIds(standingRows.stream().map(StandingRow::getTeamId).toList());
        for (StandingRow row : standingRows) {
            while (tables.size() <= row.getTableIndex())
                tables.add(new ArrayList<>());
            TeamResponse team = teamById.get(row.getTeamId());
            tables.get(row.getTableIndex())
                    .add(new StandingRowResponse(row.getTableIndex(), row.getRowIndex(), team.id(), team.name(),
                            team.logo(), team.name(), row.getRank(), row.getMatchesPlayed(), row.getWins(),
                            row.getDraws(), row.getLosses(), row.getGoalsFor(), row.getGoalsAgainst(),
                            row.getGoalDifference(), row.getPoints()));
        }
        return new StandingResponse(standing.getId(), standing.getCompetitionSeasonId(),
                competitionSeasons.get(standing.getCompetitionSeasonId()).displayName(), standing.getCreatedAt(),
                standing.getUpdatedAt(), tables);
    }
    private List<List<StandingRowResponse>> parse(JsonNode input, boolean requireResolved) {
        if (input == null || !input.isObject() || !input.has("tables") || input.size() > 2
                || !input.properties().stream().map(Map.Entry::getKey).allMatch(Set.of("tables", "mappings")::contains)
                || !input.get("tables").isArray() || input.get("tables").isEmpty())
            throw invalid();
        JsonNode mappings = input.get("mappings");
        if (mappings != null && !mappings.isArray())
            throw invalid();
        Map<String, Long> manual = new HashMap<>();
        if (mappings != null)
            for (JsonNode item : mappings) {
                if (!item.isObject() || item.size() != 3)
                    throw invalid();
                int table = integer(item.get("tableIndex"), false);
                int row = integer(item.get("rowIndex"), false);
                long team = positiveLong(item.get("teamId"));
                if (manual.put(table + ":" + row, team) != null)
                    throw invalid();
            }
        List<List<StandingRowResponse>> result = new ArrayList<>();
        JsonNode source = input.get("tables");
        for (int tableIndex = 0; tableIndex < source.size(); tableIndex++) {
            JsonNode table = source.get(tableIndex);
            if (!table.isArray() || table.isEmpty())
                throw invalid();
            List<StandingRowResponse> parsed = new ArrayList<>();
            Set<Long> seenTeams = new HashSet<>();
            for (int rowIndex = 0; rowIndex < table.size(); rowIndex++) {
                JsonNode item = table.get(rowIndex);
                if (!item.isObject() || item.size() != ROW_FIELDS.size()
                        || !item.properties().stream().map(Map.Entry::getKey).allMatch(ROW_FIELDS::contains))
                    throw invalid();
                JsonNode nameNode = item.get("team");
                if (nameNode == null || !nameNode.isTextual() || nameNode.asText().isBlank())
                    throw invalid();
                String name = nameNode.asText().trim();
                Long manualId = manual.remove(tableIndex + ":" + rowIndex);
                TeamResponse team = manualId == null ? teams.findByExactName(name).orElse(null) : teams.get(manualId);
                if (team == null && requireResolved)
                    throw invalid();
                if (team != null && !seenTeams.add(team.id()))
                    throw invalid();
                parsed.add(new StandingRowResponse(tableIndex, rowIndex, team == null ? null : team.id(),
                        team == null ? null : team.name(), team == null ? null : team.logo(), name,
                        integer(item.get("rank"), true), integer(item.get("matchesPlayed"), false),
                        integer(item.get("wins"), false), integer(item.get("draws"), false),
                        integer(item.get("losses"), false), integer(item.get("goalsFor"), false),
                        integer(item.get("goalsAgainst"), false), signed(item.get("goalDifference")),
                        signed(item.get("points"))));
            }
            result.add(parsed);
        }
        if (!manual.isEmpty())
            throw invalid();
        return result;
    }
    private int integer(JsonNode value, boolean positive) {
        int number = signed(value);
        if (positive ? number <= 0 : number < 0)
            throw invalid();
        return number;
    }
    private int signed(JsonNode value) {
        if (value == null || !value.isIntegralNumber() || !value.canConvertToInt())
            throw invalid();
        return value.intValue();
    }
    private long positiveLong(JsonNode value) {
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() <= 0)
            throw invalid();
        return value.longValue();
    }
    private BusinessException invalid() {
        return new BusinessException(HttpStatus.BAD_REQUEST, "Invalid standing data");
    }
    private BusinessException missing() {
        return new BusinessException(HttpStatus.NOT_FOUND, "Standing not found");
    }
    private BusinessException conflict() {
        return new BusinessException(HttpStatus.CONFLICT, "Standing already exists");
    }
}
