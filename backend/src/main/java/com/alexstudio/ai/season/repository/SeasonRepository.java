package com.alexstudio.ai.season.repository;

import com.alexstudio.ai.season.entity.Season;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    boolean existsByCompetition_Id(Long competitionId);

    boolean existsByCompetition_IdAndName(Long competitionId, String name);

    Optional<Season> findByIdAndCompetition_Id(Long id, Long competitionId);

    Page<Season> findByCompetition_Id(Long competitionId, Pageable pageable);
}
