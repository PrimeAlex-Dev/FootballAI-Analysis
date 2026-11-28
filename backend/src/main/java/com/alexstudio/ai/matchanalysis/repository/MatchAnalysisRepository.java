package com.alexstudio.ai.matchanalysis.repository;

import java.util.Optional;
import com.alexstudio.ai.matchanalysis.entity.MatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchAnalysisRepository extends JpaRepository<MatchAnalysis, Long> {
    Optional<MatchAnalysis> findByMatchId(Long matchId);

    boolean existsByMatchId(Long matchId);
}
