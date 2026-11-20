package com.alexstudio.ai.competition.repository;

import com.alexstudio.ai.competition.entity.Competition;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompetitionRepository extends JpaRepository<Competition, Long>, JpaSpecificationExecutor<Competition> {

    Optional<Competition> findByNameIgnoreCase(String name);
}
