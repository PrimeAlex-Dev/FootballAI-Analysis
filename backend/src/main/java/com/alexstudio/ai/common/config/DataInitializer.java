package com.alexstudio.ai.common.config;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.OffsetDateTime;

import com.alexstudio.ai.competition.entity.Competition;
import com.alexstudio.ai.competition.entity.CompetitionType;
import com.alexstudio.ai.competition.repository.CompetitionRepository;
import com.alexstudio.ai.competitionseason.entity.CompetitionSeason;
import com.alexstudio.ai.competitionseason.repository.CompetitionSeasonRepository;
import com.alexstudio.ai.season.entity.Season;
import com.alexstudio.ai.season.repository.SeasonRepository;
import com.alexstudio.ai.team.entity.Team;
import com.alexstudio.ai.team.entity.TeamType;
import com.alexstudio.ai.team.repository.TeamRepository;
import com.alexstudio.ai.match.entity.Match;
import com.alexstudio.ai.match.entity.MatchStatus;
import com.alexstudio.ai.match.repository.MatchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final CompetitionSeasonRepository competitionSeasonRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public DataInitializer(CompetitionRepository competitionRepository, SeasonRepository seasonRepository,
            CompetitionSeasonRepository competitionSeasonRepository, TeamRepository teamRepository,
            MatchRepository matchRepository) {
        this.competitionRepository = competitionRepository;
        this.seasonRepository = seasonRepository;
        this.competitionSeasonRepository = competitionSeasonRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<CompetitionSeed> competitionSeeds = List.of(
                new CompetitionSeed("Premier League", CompetitionType.LEAGUE, List.of("2025/2026", "2026/2027")),
                new CompetitionSeed("Serie A", CompetitionType.LEAGUE,
                        List.of("2023/2024", "2024/2025", "2025/2026", "2026/2027")),
                new CompetitionSeed("La Liga", CompetitionType.LEAGUE, List.of("2025/2026", "2026/2027")),
                new CompetitionSeed("FIFA World Cup", CompetitionType.CUP, List.of("2022", "2026")),
                new CompetitionSeed("UEFA EURO", CompetitionType.CUP, List.of("2024")));

        for (CompetitionSeed seed : competitionSeeds) {
            Competition competition = competitionRepository.findByNameIgnoreCase(seed.name())
                    .orElseGet(() -> competitionRepository.save(new Competition(seed.name(), seed.type(), null)));

            for (String seasonName : seed.seasons()) {
                Season season = seasonRepository.findByName(seasonName)
                        .orElseGet(() -> seasonRepository.save(new Season(seasonName)));
                if (!competitionSeasonRepository.existsByCompetition_IdAndSeason_Id(competition.getId(),
                        season.getId())) {
                    competitionSeasonRepository.save(new CompetitionSeason(competition, season));
                }
            }
        }
        seedMatches();
    }

    private void seedMatches() {
        Competition competition = competitionRepository.findByNameIgnoreCase("Serie A").orElseThrow();
        Map<String, CompetitionSeason> competitionSeasons = new HashMap<>();
        for (String seasonName : List.of("2023/2024", "2024/2025", "2025/2026", "2026/2027")) {
            Season season = seasonRepository.findByName(seasonName).orElseThrow();
            CompetitionSeason link = competitionSeasonRepository
                    .findByCompetition_IdAndSeason_Id(competition.getId(), season.getId()).orElseThrow();
            competitionSeasons.put(seasonName, link);
        }
        Map<String, Team> teams = new HashMap<>();
        for (String name : List.of("Inter", "Fiorentina", "AC Milan", "Napoli")) {
            Team team = teamRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> teamRepository.save(new Team(name, TeamType.CLUB, null)));
            teams.put(name, team);
        }

        List<MatchSeed> matchSeeds = List.of(
                new MatchSeed("2026/2027", "Inter", "Fiorentina", "2026-11-27T23:30:00+07:00", MatchStatus.SCHEDULED,
                        null, null),
                new MatchSeed("2026/2027", "Inter", "Fiorentina", "2026-10-25T20:00:00+07:00", MatchStatus.FINISHED, 2,
                        1),
                new MatchSeed("2025/2026", "Fiorentina", "Inter", "2026-04-12T20:00:00+07:00", MatchStatus.FINISHED, 1,
                        1),
                new MatchSeed("2025/2026", "Inter", "Fiorentina", "2025-11-10T20:00:00+07:00", MatchStatus.FINISHED, 3,
                        0),
                new MatchSeed("2024/2025", "Fiorentina", "Inter", "2025-03-15T20:00:00+07:00", MatchStatus.FINISHED, 2,
                        1),
                new MatchSeed("2024/2025", "Inter", "Fiorentina", "2024-09-10T20:00:00+07:00", MatchStatus.FINISHED, 0,
                        0),
                new MatchSeed("2023/2024", "Fiorentina", "Inter", "2024-02-25T20:00:00+07:00", MatchStatus.FINISHED, 0,
                        2),
                new MatchSeed("2026/2027", "Inter", "AC Milan", "2026-11-18T20:00:00+07:00", MatchStatus.FINISHED, 1,
                        0),
                new MatchSeed("2026/2027", "Napoli", "Inter", "2026-11-02T20:00:00+07:00", MatchStatus.FINISHED, 2, 2),
                new MatchSeed("2026/2027", "Fiorentina", "Napoli", "2026-11-20T20:00:00+07:00", MatchStatus.FINISHED, 2,
                        0),
                new MatchSeed("2026/2027", "AC Milan", "Fiorentina", "2026-11-02T20:00:00+07:00", MatchStatus.FINISHED,
                        1, 2));

        for (MatchSeed seed : matchSeeds) {
            Long competitionSeasonId = competitionSeasons.get(seed.seasonName()).getId();
            Long homeId = teams.get(seed.home()).getId();
            Long awayId = teams.get(seed.away()).getId();
            var kickoff = OffsetDateTime.parse(seed.kickoff()).toInstant();
            if (!matchRepository.existsByCompetitionSeasonIdAndHomeTeamIdAndAwayTeamIdAndKickoffTime(
                    competitionSeasonId, homeId, awayId, kickoff)) {
                matchRepository.save(new Match(competitionSeasonId, homeId, awayId, kickoff, seed.status(),
                        seed.homeScore(), seed.awayScore(), null));
            }
        }
    }

    private record CompetitionSeed(String name, CompetitionType type, List<String> seasons) {
    }

    private record MatchSeed(String seasonName, String home, String away, String kickoff, MatchStatus status,
            Integer homeScore, Integer awayScore) {
    }
}
