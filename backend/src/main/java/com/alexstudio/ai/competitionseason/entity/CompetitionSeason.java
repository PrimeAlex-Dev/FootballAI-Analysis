package com.alexstudio.ai.competitionseason.entity;

import com.alexstudio.ai.competition.entity.Competition;
import com.alexstudio.ai.season.entity.Season;
import jakarta.persistence.*;

@Entity
@Table(name = "competition_seasons")
public class CompetitionSeason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    protected CompetitionSeason() {
    }

    public CompetitionSeason(Competition competition, Season season) {
        this.competition = competition;
        this.season = season;
    }

    public Long getId() {
        return id;
    }
    public Competition getCompetition() {
        return competition;
    }
    public Season getSeason() {
        return season;
    }
}
