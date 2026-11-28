package com.alexstudio.ai.standing.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "standing_rows")
public class StandingRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long standingId;
    @Column(nullable = false)
    private int tableIndex;
    @Column(nullable = false)
    private int rowIndex;
    @Column(nullable = false)
    private Long teamId;
    @Column(nullable = false)
    private int rank;
    @Column(nullable = false)
    private int matchesPlayed;
    @Column(nullable = false)
    private int wins;
    @Column(nullable = false)
    private int draws;
    @Column(nullable = false)
    private int losses;
    @Column(nullable = false)
    private int goalsFor;
    @Column(nullable = false)
    private int goalsAgainst;
    @Column(nullable = false)
    private int goalDifference;
    @Column(nullable = false)
    private int points;
    protected StandingRow() {
    }
    public StandingRow(Long standingId, int tableIndex, int rowIndex, Long teamId, int rank, int matchesPlayed,
            int wins, int draws, int losses, int goalsFor, int goalsAgainst, int goalDifference, int points) {
        this.standingId = standingId;
        this.tableIndex = tableIndex;
        this.rowIndex = rowIndex;
        this.teamId = teamId;
        this.rank = rank;
        this.matchesPlayed = matchesPlayed;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalDifference = goalDifference;
        this.points = points;
    }
    public Long getStandingId() {
        return standingId;
    }
    public int getTableIndex() {
        return tableIndex;
    }
    public int getRowIndex() {
        return rowIndex;
    }
    public Long getTeamId() {
        return teamId;
    }
    public int getRank() {
        return rank;
    }
    public int getMatchesPlayed() {
        return matchesPlayed;
    }
    public int getWins() {
        return wins;
    }
    public int getDraws() {
        return draws;
    }
    public int getLosses() {
        return losses;
    }
    public int getGoalsFor() {
        return goalsFor;
    }
    public int getGoalsAgainst() {
        return goalsAgainst;
    }
    public int getGoalDifference() {
        return goalDifference;
    }
    public int getPoints() {
        return points;
    }
}
