package com.alexstudio.ai.match.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.alexstudio.ai.match.dto.MatchAnalysisPromptResponse;
import com.alexstudio.ai.match.dto.MatchResponse;
import com.alexstudio.ai.match.repository.MatchRepository;
import com.alexstudio.ai.matchstatistics.entity.MatchStatistics;
import com.alexstudio.ai.matchstatistics.entity.StatisticsSide;
import com.alexstudio.ai.matchstatistics.repository.MatchStatisticsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchAnalysisPromptService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final Sort NEWEST = Sort.by(Sort.Direction.DESC, "kickoffTime", "id");
    private final MatchService matches;
    private final MatchRepository repository;
    private final MatchStatisticsRepository statistics;

    public MatchAnalysisPromptService(MatchService matches, MatchRepository repository,
            MatchStatisticsRepository statistics) {
        this.matches = matches;
        this.repository = repository;
        this.statistics = statistics;
    }

    @Transactional(readOnly = true)
    public MatchAnalysisPromptResponse export(Long id) {
        MatchResponse target = matches.get(id);
        Instant before = target.kickoffTime();
        List<MatchResponse> home = repository.recentForPrompt(target.homeTeamId(), before,
                PageRequest.of(0, 5, NEWEST));
        List<MatchResponse> away = repository.recentForPrompt(target.awayTeamId(), before,
                PageRequest.of(0, 5, NEWEST));
        List<MatchResponse> h2h = repository.headToHeadForPrompt(target.homeTeamId(), target.awayTeamId(), before,
                PageRequest.of(0, 5, NEWEST));
        List<Long> historyIds = Stream.concat(home.stream(), away.stream()).map(MatchResponse::id).distinct().toList();
        Map<Long, MatchStatistics> stats = historyIds.isEmpty()
                ? Map.of()
                : statistics.findByMatchIdIn(historyIds).stream()
                        .collect(Collectors.toMap(MatchStatistics::getMatchId, Function.identity()));

        StringBuilder prompt = new StringBuilder();
        prompt.append("PHẦN 1 — THÔNG TIN TRẬN ĐẤU\n");
        prompt.append(target.homeTeamName()).append(" vs ").append(target.awayTeamName()).append('\n');
        prompt.append("Giải đấu: ").append(target.competitionSeasonName()).append('\n');
        prompt.append("Thời gian: ").append(TIME.format(target.kickoffTime())).append(" (giờ Việt Nam)\n\n");
        appendForm(prompt, "PHẦN 2 — PHONG ĐỘ ĐỘI NHÀ", target.homeTeamName(), target.homeTeamId(), home, stats);
        appendForm(prompt, "PHẦN 3 — PHONG ĐỘ ĐỘI KHÁCH", target.awayTeamName(), target.awayTeamId(), away, stats);
        prompt.append("PHẦN 4 — LỊCH SỬ ĐỐI ĐẦU\n");
        if (h2h.isEmpty())
            prompt.append("Không có dữ liệu đối đầu trước trận mục tiêu.\n");
        else
            for (int index = 0; index < h2h.size(); index++)
                prompt.append(index + 1).append(". ").append(matchLine(h2h.get(index))).append('\n');
        prompt.append("\nPHẦN 5 — TÌNH TRẠNG DỮ LIỆU\n");
        appendCoverage(prompt, target.homeTeamName(), home, stats);
        appendCoverage(prompt, target.awayTeamName(), away, stats);
        return new MatchAnalysisPromptResponse(prompt.toString(), home.size(), away.size(), countStats(home, stats),
                countStats(away, stats));
    }

    private void appendForm(StringBuilder out, String heading, String teamName, Long teamId,
            List<MatchResponse> history, Map<Long, MatchStatistics> stats) {
        out.append(heading).append(" — ").append(teamName).append('\n');
        out.append("Dữ liệu hiện có: ").append(history.size()).append("/5 trận\n");
        if (history.isEmpty()) {
            out.append("Không có trận đã kết thúc trước trận mục tiêu.\n\n");
            return;
        }
        String form = history.stream().map(match -> outcome(match, teamId)).collect(Collectors.joining("-"));
        int goalsFor = history.stream()
                .mapToInt(match -> match.homeTeamId().equals(teamId) ? match.homeScore() : match.awayScore()).sum();
        int goalsAgainst = history.stream()
                .mapToInt(match -> match.homeTeamId().equals(teamId) ? match.awayScore() : match.homeScore()).sum();
        out.append("Phong độ (mới nhất → cũ nhất): ").append(form).append('\n');
        out.append("Tổng bàn thắng: ").append(goalsFor).append('\n');
        out.append("Tổng bàn thua: ").append(goalsAgainst).append("\n\n");
        for (int index = 0; index < history.size(); index++) {
            MatchResponse match = history.get(index);
            out.append(index + 1).append(". ").append(matchLine(match)).append(" — ").append(teamName).append(": ")
                    .append(outcome(match, teamId)).append('\n');
            MatchStatistics value = stats.get(match.id());
            if (value == null)
                out.append("   Chưa có Match Statistics.\n");
            else {
                appendSide(out, match.homeTeamName(), value.getHome());
                appendSide(out, match.awayTeamName(), value.getAway());
            }
            out.append('\n');
        }
    }

    private String matchLine(MatchResponse match) {
        return TIME.format(match.kickoffTime()) + " — " + match.competitionSeasonName() + " — " + match.homeTeamName()
                + " " + match.homeScore() + "–" + match.awayScore() + " " + match.awayTeamName();
    }

    private String outcome(MatchResponse match, Long teamId) {
        int own = match.homeTeamId().equals(teamId) ? match.homeScore() : match.awayScore();
        int other = match.homeTeamId().equals(teamId) ? match.awayScore() : match.homeScore();
        return own > other ? "W" : own < other ? "L" : "D";
    }

    private void appendSide(StringBuilder out, String team, StatisticsSide side) {
        out.append("   Thống kê ").append(team).append(": ");
        StringBuilder values = new StringBuilder();
        if (side != null) {
            stat(values, "Kiểm soát bóng", side.getPossession(), "%");
            stat(values, "Sút", side.getShots(), "");
            stat(values, "Sút trúng đích", side.getShotsOnTarget(), "");
            stat(values, "Phạt góc", side.getCorners(), "");
            stat(values, "Thẻ vàng", side.getYellowCards(), "");
            stat(values, "Thẻ đỏ", side.getRedCards(), "");
            stat(values, "xG", side.getXg(), "");
            stat(values, "Cơ hội lớn", side.getBigChances(), "");
            stat(values, "Chạm bóng trong vòng cấm", side.getTouchesInBox(), "");
            stat(values, "Chính xác chuyền bóng", side.getPassAccuracy(), "%");
            stat(values, "Chuyền vào 1/3 cuối sân", side.getPassesFinalThird(), "");
            stat(values, "Chuyền quyết định", side.getKeyPasses(), "");
            stat(values, "Chuyền dài", side.getLongPasses(), "");
            stat(values, "Tạt bóng", side.getCrosses(), "");
            stat(values, "Cắt bóng", side.getInterceptions(), "");
            stat(values, "Giải nguy", side.getClearances(), "");
        }
        out.append(values.isEmpty() ? "Không có chỉ số được cung cấp." : values.toString()).append('\n');
    }

    private void stat(StringBuilder out, String label, Number value, String unit) {
        if (value == null)
            return;
        if (!out.isEmpty())
            out.append("; ");
        out.append(label).append(' ');
        out.append(value instanceof BigDecimal decimal ? decimal.toPlainString() : value);
        out.append(unit);
    }

    private int countStats(List<MatchResponse> history, Map<Long, MatchStatistics> stats) {
        return (int) history.stream().filter(match -> stats.containsKey(match.id())).count();
    }

    private void appendCoverage(StringBuilder out, String team, List<MatchResponse> history,
            Map<Long, MatchStatistics> stats) {
        out.append(team).append(": ").append(history.size()).append("/5 trận lịch sử; ")
                .append(countStats(history, stats)).append('/').append(history.size())
                .append(" trận có Match Statistics.\n");
        for (MatchResponse match : history)
            if (!stats.containsKey(match.id()))
                out.append("Thiếu Match Statistics: ").append(matchLine(match)).append('\n');
    }
}
