package com.alexstudio.ai.matchstatistics.repository;
import java.util.Optional;
import com.alexstudio.ai.matchstatistics.entity.MatchStatistics;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface MatchStatisticsRepository extends JpaRepository<MatchStatistics, Long> {
    Optional<MatchStatistics> findByMatchId(Long matchId);
    boolean existsByMatchId(Long matchId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from MatchStatistics s where s.matchId = :matchId")
    Optional<MatchStatistics> findByMatchIdForUpdate(Long matchId);
}
