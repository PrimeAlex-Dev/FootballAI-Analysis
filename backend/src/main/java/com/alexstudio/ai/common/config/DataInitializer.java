package com.alexstudio.ai.common.config;

import java.util.List;

import com.alexstudio.ai.competition.entity.Competition;
import com.alexstudio.ai.competition.entity.CompetitionType;
import com.alexstudio.ai.competition.repository.CompetitionRepository;
import com.alexstudio.ai.competitionseason.entity.CompetitionSeason;
import com.alexstudio.ai.competitionseason.repository.CompetitionSeasonRepository;
import com.alexstudio.ai.season.entity.Season;
import com.alexstudio.ai.season.repository.SeasonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final CompetitionSeasonRepository competitionSeasonRepository;

    public DataInitializer(CompetitionRepository competitionRepository, SeasonRepository seasonRepository,
            CompetitionSeasonRepository competitionSeasonRepository) {
        this.competitionRepository = competitionRepository;
        this.seasonRepository = seasonRepository;
        this.competitionSeasonRepository = competitionSeasonRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<CompetitionSeed> competitionSeeds = List.of(
                new CompetitionSeed("Premier League", CompetitionType.LEAGUE, List.of("2025/2026", "2026/2027")),
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
    }

    private record CompetitionSeed(String name, CompetitionType type, List<String> seasons) {
    }
}
