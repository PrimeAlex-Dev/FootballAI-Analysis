package com.alexstudio.ai.matchstatistics.entity;
import java.time.Instant;
import jakarta.persistence.*;
@Entity
@Table(name = "match_statistics")
public class MatchStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "match_id", nullable = false, unique = true)
    private Long matchId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "bigChances", column = @Column(name = "home_big_chances")),
            @AttributeOverride(name = "clearances", column = @Column(name = "home_clearances")),
            @AttributeOverride(name = "corners", column = @Column(name = "home_corners")),
            @AttributeOverride(name = "crosses", column = @Column(name = "home_crosses")),
            @AttributeOverride(name = "interceptions", column = @Column(name = "home_interceptions")),
            @AttributeOverride(name = "keyPasses", column = @Column(name = "home_key_passes")),
            @AttributeOverride(name = "longPasses", column = @Column(name = "home_long_passes")),
            @AttributeOverride(name = "passAccuracy", column = @Column(name = "home_pass_accuracy")),
            @AttributeOverride(name = "passesFinalThird", column = @Column(name = "home_passes_final_third")),
            @AttributeOverride(name = "possession", column = @Column(name = "home_possession")),
            @AttributeOverride(name = "redCards", column = @Column(name = "home_red_cards")),
            @AttributeOverride(name = "shots", column = @Column(name = "home_shots")),
            @AttributeOverride(name = "shotsOnTarget", column = @Column(name = "home_shots_on_target")),
            @AttributeOverride(name = "touchesInBox", column = @Column(name = "home_touches_in_box")),
            @AttributeOverride(name = "xg", column = @Column(name = "home_xg")),
            @AttributeOverride(name = "yellowCards", column = @Column(name = "home_yellow_cards"))})
    private StatisticsSide home;
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "bigChances", column = @Column(name = "away_big_chances")),
            @AttributeOverride(name = "clearances", column = @Column(name = "away_clearances")),
            @AttributeOverride(name = "corners", column = @Column(name = "away_corners")),
            @AttributeOverride(name = "crosses", column = @Column(name = "away_crosses")),
            @AttributeOverride(name = "interceptions", column = @Column(name = "away_interceptions")),
            @AttributeOverride(name = "keyPasses", column = @Column(name = "away_key_passes")),
            @AttributeOverride(name = "longPasses", column = @Column(name = "away_long_passes")),
            @AttributeOverride(name = "passAccuracy", column = @Column(name = "away_pass_accuracy")),
            @AttributeOverride(name = "passesFinalThird", column = @Column(name = "away_passes_final_third")),
            @AttributeOverride(name = "possession", column = @Column(name = "away_possession")),
            @AttributeOverride(name = "redCards", column = @Column(name = "away_red_cards")),
            @AttributeOverride(name = "shots", column = @Column(name = "away_shots")),
            @AttributeOverride(name = "shotsOnTarget", column = @Column(name = "away_shots_on_target")),
            @AttributeOverride(name = "touchesInBox", column = @Column(name = "away_touches_in_box")),
            @AttributeOverride(name = "xg", column = @Column(name = "away_xg")),
            @AttributeOverride(name = "yellowCards", column = @Column(name = "away_yellow_cards"))})
    private StatisticsSide away;
    protected MatchStatistics() {
    }
    public MatchStatistics(Long matchId, StatisticsSide home, StatisticsSide away, Instant now) {
        this.matchId = matchId;
        this.createdAt = now;
        replace(home, away, now);
    }
    public void replace(StatisticsSide home, StatisticsSide away, Instant now) {
        this.home = home;
        this.away = away;
        this.updatedAt = now;
    }
    public Long getId() {
        return id;
    }
    public Long getMatchId() {
        return matchId;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public StatisticsSide getHome() {
        return home;
    }
    public StatisticsSide getAway() {
        return away;
    }
}
