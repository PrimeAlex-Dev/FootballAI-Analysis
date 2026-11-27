package com.alexstudio.ai.competitionseason.repository;

import com.alexstudio.ai.competitionseason.entity.CompetitionSeason;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompetitionSeasonRepository
        extends
            JpaRepository<CompetitionSeason, Long>,
            JpaSpecificationExecutor<CompetitionSeason> {
    boolean existsByCompetition_Id(Long competitionId);
    boolean existsBySeason_Id(Long seasonId);
    boolean existsByCompetition_IdAndSeason_Id(Long competitionId, Long seasonId);
    Optional<CompetitionSeason> findByCompetition_IdAndSeason_Id(Long competitionId, Long seasonId);

    @Override
    @EntityGraph(attributePaths = {"competition", "season"})
    Page<CompetitionSeason> findAll(org.springframework.data.jpa.domain.Specification<CompetitionSeason> spec,
            Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"competition", "season"})
    java.util.Optional<CompetitionSeason> findById(Long id);
}
