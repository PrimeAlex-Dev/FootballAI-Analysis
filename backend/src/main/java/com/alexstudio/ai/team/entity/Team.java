package com.alexstudio.ai.team.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamType type;

    @Column(columnDefinition = "text")
    private String logo;

    protected Team() {
    }

    public Team(String name, TeamType type, String logo) {
        this.name = name;
        this.type = type;
        this.logo = logo;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public TeamType getType() {
        return type;
    }
    public String getLogo() {
        return logo;
    }

    public void update(String name, TeamType type, String logo) {
        this.name = name;
        this.type = type;
        this.logo = logo;
    }
}
