package com.alexstudio.ai.season.entity;

import com.alexstudio.ai.competition.entity.Competition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seasons")
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @Column(nullable = false, columnDefinition = "text")
    private String name;

    protected Season() {
    }

    public Season(Competition competition, String name) {
        this.competition = competition;
        this.name = name;
    }

    public Long getId() {
        return id;
    }
    public Long getCompetitionId() {
        return competition.getId();
    }
    public String getName() {
        return name;
    }
    public void rename(String name) {
        this.name = name;
    }
}
