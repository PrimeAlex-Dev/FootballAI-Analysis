package com.alexstudio.ai.match.entity;
import java.time.Instant;
import jakarta.persistence.*;
@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long competitionSeasonId;
    @Column(nullable = false)
    private Long homeTeamId;
    @Column(nullable = false)
    private Long awayTeamId;
    @Column(nullable = false)
    private Instant kickoffTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status;
    private Integer homeScore;
    private Integer awayScore;
    @Column(columnDefinition = "text")
    private String round;
    protected Match() {
    }
    public Match(Long competitionSeasonId, Long homeTeamId, Long awayTeamId, Instant kickoffTime, MatchStatus status,
            Integer homeScore, Integer awayScore, String round) {
        update(competitionSeasonId, homeTeamId, awayTeamId, kickoffTime, status, homeScore, awayScore, round);
    }
    public void update(Long competitionSeasonId, Long homeTeamId, Long awayTeamId, Instant kickoffTime,
            MatchStatus status, Integer homeScore, Integer awayScore, String round) {
        this.competitionSeasonId = competitionSeasonId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.kickoffTime = kickoffTime;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.round = round;
    }
    public Long getId() {
        return id;
    }
    public Long getCompetitionSeasonId() {
        return competitionSeasonId;
    }
    public Long getHomeTeamId() {
        return homeTeamId;
    }
    public Long getAwayTeamId() {
        return awayTeamId;
    }
    public Instant getKickoffTime() {
        return kickoffTime;
    }
    public MatchStatus getStatus() {
        return status;
    }
    public Integer getHomeScore() {
        return homeScore;
    }
    public Integer getAwayScore() {
        return awayScore;
    }
    public String getRound() {
        return round;
    }
}
