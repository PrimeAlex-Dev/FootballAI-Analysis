package com.alexstudio.ai.matchstatistics.service;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.match.service.MatchService;
import com.alexstudio.ai.matchstatistics.entity.*;
import com.alexstudio.ai.matchstatistics.repository.MatchStatisticsRepository;
import com.alexstudio.ai.matchstatistics.dto.StatisticsResponse;
import tools.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class MatchStatisticsService {
    private static final Set<String> FIELDS = Set.of("bigChances", "clearances", "corners", "crosses", "interceptions",
            "keyPasses", "longPasses", "passAccuracy", "passesFinalThird", "possession", "redCards", "shots",
            "shotsOnTarget", "touchesInBox", "xg", "yellowCards");
    private final MatchStatisticsRepository repository;
    private final MatchService matches;
    private final Clock clock;
    public MatchStatisticsService(MatchStatisticsRepository repository, MatchService matches, Clock clock) {
        this.repository = repository;
        this.matches = matches;
        this.clock = clock;
    }
    @Transactional(readOnly = true)
    public StatisticsResponse get(Long matchId) {
        matches.get(matchId);
        return response(repository.findByMatchId(matchId).orElseThrow(() -> missing()));
    }
    @Transactional(readOnly = true)
    public StatisticsResponse preview(Long matchId, JsonNode input) {
        matches.get(matchId);
        StatisticsSide[] sides = parse(input);
        return new StatisticsResponse(matchId, sides[0], sides[1], null, null);
    }
    @Transactional
    public StatisticsResponse create(Long matchId, JsonNode input) {
        matches.get(matchId);
        StatisticsSide[] sides = parse(input);
        if (repository.existsByMatchId(matchId))
            throw new BusinessException(HttpStatus.CONFLICT, "Match statistics already exists");
        return response(repository.saveAndFlush(
                new MatchStatistics(matchId, sides[0], sides[1], clock.instant().truncatedTo(ChronoUnit.MICROS))));
    }
    @Transactional
    public StatisticsResponse update(Long matchId, JsonNode input) {
        matches.get(matchId);
        StatisticsSide[] sides = parse(input);
        MatchStatistics stats = repository.findByMatchIdForUpdate(matchId).orElseThrow(() -> missing());
        stats.replace(sides[0], sides[1], clock.instant().truncatedTo(ChronoUnit.MICROS));
        repository.flush();
        return response(stats);
    }
    private StatisticsResponse response(MatchStatistics stats) {
        return new StatisticsResponse(stats.getMatchId(),
                stats.getHome() == null ? new StatisticsSide() : stats.getHome(),
                stats.getAway() == null ? new StatisticsSide() : stats.getAway(), stats.getCreatedAt(),
                stats.getUpdatedAt());
    }
    private StatisticsSide[] parse(JsonNode input) {
        if (input == null || !input.isObject() || input.size() != 2 || !input.has("home") || !input.has("away"))
            throw invalid();
        return new StatisticsSide[]{parseSide(input.get("home")), parseSide(input.get("away"))};
    }
    private StatisticsSide parseSide(JsonNode data) {
        if (data == null || !data.isObject())
            throw invalid();
        StatisticsSide side = new StatisticsSide();
        var fields = data.properties().iterator();
        while (fields.hasNext()) {
            var entry = fields.next();
            if (!FIELDS.contains(entry.getKey()))
                throw invalid();
            JsonNode value = entry.getValue();
            if (value.isNull())
                continue;
            switch (entry.getKey()) {
                case "bigChances" -> side.setBigChances(integer(value, false));
                case "clearances" -> side.setClearances(integer(value, false));
                case "corners" -> side.setCorners(integer(value, false));
                case "crosses" -> side.setCrosses(integer(value, false));
                case "interceptions" -> side.setInterceptions(integer(value, false));
                case "keyPasses" -> side.setKeyPasses(integer(value, false));
                case "longPasses" -> side.setLongPasses(integer(value, false));
                case "passAccuracy" -> side.setPassAccuracy(integer(value, true));
                case "passesFinalThird" -> side.setPassesFinalThird(integer(value, false));
                case "possession" -> side.setPossession(integer(value, true));
                case "redCards" -> side.setRedCards(integer(value, false));
                case "shots" -> side.setShots(integer(value, false));
                case "shotsOnTarget" -> side.setShotsOnTarget(integer(value, false));
                case "touchesInBox" -> side.setTouchesInBox(integer(value, false));
                case "xg" -> side.setXg(decimal(value));
                case "yellowCards" -> side.setYellowCards(integer(value, false));
                default -> throw invalid();
            }
        }
        return side;
    }
    private Integer integer(JsonNode value, boolean percent) {
        if (!value.isIntegralNumber() || !value.canConvertToInt())
            throw invalid();
        int number = value.intValue();
        if (number < 0 || (percent && number > 100))
            throw invalid();
        return number;
    }
    private BigDecimal decimal(JsonNode value) {
        if (!value.isNumber())
            throw invalid();
        BigDecimal number = value.decimalValue();
        if (number.signum() < 0 || number.scale() > 2 || number.precision() > 10)
            throw invalid();
        return number;
    }
    private BusinessException invalid() {
        return new BusinessException(HttpStatus.BAD_REQUEST, "Invalid match statistics");
    }
    private BusinessException missing() {
        return new BusinessException(HttpStatus.NOT_FOUND, "Match statistics not found");
    }
}
