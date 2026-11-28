package com.alexstudio.ai.match.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.alexstudio.ai.match.dto.MatchResponse;
import com.alexstudio.ai.match.dto.PlayingStyleRadarResponse;
import com.alexstudio.ai.match.dto.PlayingStyleRadarResponse.RadarAxis;
import com.alexstudio.ai.match.dto.PlayingStyleRadarResponse.RadarMetric;
import com.alexstudio.ai.match.dto.PlayingStyleRadarResponse.RadarTeam;
import com.alexstudio.ai.match.entity.MatchStatus;
import com.alexstudio.ai.match.repository.MatchRepository;
import com.alexstudio.ai.matchstatistics.entity.MatchStatistics;
import com.alexstudio.ai.matchstatistics.entity.StatisticsSide;
import com.alexstudio.ai.matchstatistics.repository.MatchStatisticsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayingStyleRadarService {
    private static final int SAMPLE_LIMIT = 5;
    private static final int MIN_METRIC_MATCHES = 2;
    private static final int MIN_REFERENCE_PROFILES = 5;
    private static final Sort NEWEST = Sort.by(Sort.Direction.DESC, "kickoffTime", "id");
    private static final String METHOD = "Phân vị thực nghiệm của hồ sơ tối đa 5 trận gần nhất; thử nghiệm, không đo sức mạnh tuyệt đối";
    private final MatchService matches;
    private final MatchRepository repository;
    private final MatchStatisticsRepository statistics;
    private final Clock clock;

    public PlayingStyleRadarService(MatchService matches, MatchRepository repository,
            MatchStatisticsRepository statistics, Clock clock) {
        this.matches = matches;
        this.repository = repository;
        this.statistics = statistics;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PlayingStyleRadarResponse get(Long matchId) {
        MatchResponse target = matches.getPublic(matchId);
        if (target.status() != MatchStatus.SCHEDULED || !target.kickoffTime().isAfter(clock.instant())) {
            return new PlayingStyleRadarResponse("NOT_APPLICABLE", SAMPLE_LIMIT, METHOD,
                    new RadarTeam(target.homeTeamId(), target.homeTeamName(), 0, List.of()),
                    new RadarTeam(target.awayTeamId(), target.awayTeamName(), 0, List.of()));
        }

        List<MatchResponse> homeHistory = recent(target.homeTeamId(), target);
        List<MatchResponse> awayHistory = recent(target.awayTeamId(), target);
        List<Object[]> appearances = repository.recentAppearancesForRadar(target.kickoffTime());
        List<Long> matchIds = appearances.stream().map(row -> ((Number) row[1]).longValue()).distinct().toList();
        Map<Long, MatchStatistics> stats = matchIds.isEmpty()
                ? Map.of()
                : statistics.findByMatchIdIn(matchIds).stream()
                        .collect(Collectors.toMap(MatchStatistics::getMatchId, Function.identity()));
        Map<Long, List<Game>> profiles = referenceProfiles(appearances, stats);
        RadarTeam home = team(target.homeTeamId(), target.homeTeamName(), homeHistory, stats, profiles);
        RadarTeam away = team(target.awayTeamId(), target.awayTeamName(), awayHistory, stats, profiles);
        String status = status(home, away);
        return new PlayingStyleRadarResponse(status, SAMPLE_LIMIT, METHOD, home, away);
    }

    private List<MatchResponse> recent(Long teamId, MatchResponse target) {
        return repository.recentForm(teamId, target.kickoffTime(), PageRequest.of(0, SAMPLE_LIMIT, NEWEST));
    }

    private Map<Long, List<Game>> referenceProfiles(List<Object[]> appearances, Map<Long, MatchStatistics> stats) {
        Map<Long, List<Game>> profiles = new HashMap<>();
        for (Object[] row : appearances) {
            Long teamId = ((Number) row[0]).longValue();
            Long matchId = ((Number) row[1]).longValue();
            boolean home = (Boolean) row[2];
            profiles.computeIfAbsent(teamId, ignored -> new ArrayList<>()).add(game(stats.get(matchId), home));
        }
        return profiles;
    }

    private RadarTeam team(Long teamId, String name, List<MatchResponse> history, Map<Long, MatchStatistics> stats,
            Map<Long, List<Game>> profiles) {
        List<Game> games = new ArrayList<>();
        for (MatchResponse match : history)
            games.add(game(stats.get(match.id()), match.homeTeamId().equals(teamId)));
        List<RadarAxis> axes = new ArrayList<>();
        axes.add(axis("BALL_CONTROL", "Kiểm soát bóng", Metric.POSSESSION, false, List.of(Metric.PASS_ACCURACY), games,
                profiles));
        axes.add(axis("ATTACKING_PROGRESSION", "Triển khai tấn công", Metric.PASSES_FINAL_THIRD, false, List.of(),
                games, profiles));
        axes.add(axis("CHANCE_CREATION", "Tạo cơ hội", Metric.XG, false, List.of(Metric.BIG_CHANCES, Metric.KEY_PASSES),
                games, profiles));
        axes.add(axis("SHOOTING_ACTIVITY", "Dứt điểm", Metric.SHOTS_ON_TARGET, false, List.of(Metric.SHOTS), games,
                profiles));
        axes.add(
                axis("BOX_PENETRATION", "Xâm nhập vòng cấm", Metric.TOUCHES_IN_BOX, false, List.of(), games, profiles));
        axes.add(axis("CHANCE_SUPPRESSION", "Hạn chế cơ hội đối thủ", Metric.XG, true, List.of(Metric.SHOTS), games,
                profiles));
        return new RadarTeam(teamId, name, history.size(), axes);
    }

    private RadarAxis axis(String key, String label, Metric primary, boolean opponent, List<Metric> supporting,
            List<Game> games, Map<Long, List<Game>> profiles) {
        RadarMetric value = average(primary, opponent, games);
        List<Double> reference = new ArrayList<>();
        for (List<Game> profile : profiles.values()) {
            RadarMetric metric = average(primary, opponent, profile);
            if (metric.availableMatches() >= MIN_METRIC_MATCHES)
                reference.add(metric.average());
        }
        Double score = null;
        if (value.availableMatches() >= MIN_METRIC_MATCHES && reference.size() >= MIN_REFERENCE_PROFILES) {
            double below = 0;
            double equal = 0;
            for (double item : reference) {
                if (item < value.average())
                    below++;
                else if (item == value.average())
                    equal++;
            }
            score = 100.0 * (below + 0.5 * equal) / reference.size();
            if (opponent)
                score = 100.0 - score;
        }
        List<RadarMetric> details = supporting.stream().map(metric -> average(metric, opponent, games)).toList();
        return new RadarAxis(key, label, score, value, details, reference.size());
    }

    private RadarMetric average(Metric metric, boolean opponent, List<Game> games) {
        double sum = 0;
        int count = 0;
        for (Game game : games) {
            StatisticsSide side = opponent ? game.opponent() : game.own();
            Number value = side == null ? null : metric.read(side);
            if (value != null) {
                sum += value.doubleValue();
                count++;
            }
        }
        return new RadarMetric(metric.label, count == 0 ? null : sum / count, count, metric.unit);
    }

    private Game game(MatchStatistics value, boolean home) {
        if (value == null)
            return new Game(null, null);
        return home ? new Game(value.getHome(), value.getAway()) : new Game(value.getAway(), value.getHome());
    }

    private String status(RadarTeam home, RadarTeam away) {
        boolean missingScore = false;
        for (int index = 0; index < home.axes().size(); index++) {
            RadarAxis homeAxis = home.axes().get(index);
            RadarAxis awayAxis = away.axes().get(index);
            if (homeAxis.referenceProfiles() < MIN_REFERENCE_PROFILES)
                return "INSUFFICIENT_REFERENCE_DATA";
            if (homeAxis.score() == null || awayAxis.score() == null)
                missingScore = true;
        }
        return missingScore ? "PARTIAL" : "READY";
    }

    private record Game(StatisticsSide own, StatisticsSide opponent) {
    }

    private enum Metric {
        POSSESSION("Kiểm soát bóng", "%", StatisticsSide::getPossession), PASS_ACCURACY("Chính xác chuyền bóng", "%",
                StatisticsSide::getPassAccuracy), PASSES_FINAL_THIRD("Chuyền vào 1/3 cuối sân", "",
                        StatisticsSide::getPassesFinalThird), XG("xG", "", StatisticsSide::getXg), BIG_CHANCES(
                                "Cơ hội lớn", "", StatisticsSide::getBigChances), KEY_PASSES("Chuyền quyết định", "",
                                        StatisticsSide::getKeyPasses), SHOTS_ON_TARGET("Sút trúng đích", "",
                                                StatisticsSide::getShotsOnTarget), SHOTS("Sút", "",
                                                        StatisticsSide::getShots), TOUCHES_IN_BOX(
                                                                "Chạm bóng trong vòng cấm", "",
                                                                StatisticsSide::getTouchesInBox);

        private final String label;
        private final String unit;
        private final Function<StatisticsSide, Number> reader;

        Metric(String label, String unit, Function<StatisticsSide, Number> reader) {
            this.label = label;
            this.unit = unit;
            this.reader = reader;
        }

        Number read(StatisticsSide side) {
            return reader.apply(side);
        }
    }
}
