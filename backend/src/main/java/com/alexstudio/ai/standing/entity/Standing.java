package com.alexstudio.ai.standing.entity;
import java.time.Instant;
import jakarta.persistence.*;
@Entity
@Table(name = "standings")
public class Standing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long competitionSeasonId;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;
    protected Standing() {
    }
    public Standing(Long competitionSeasonId, Instant now) {
        this.competitionSeasonId = competitionSeasonId;
        this.createdAt = now;
        this.updatedAt = now;
    }
    public void touch(Instant now) {
        this.updatedAt = now;
    }
    public Long getId() {
        return id;
    }
    public Long getCompetitionSeasonId() {
        return competitionSeasonId;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
