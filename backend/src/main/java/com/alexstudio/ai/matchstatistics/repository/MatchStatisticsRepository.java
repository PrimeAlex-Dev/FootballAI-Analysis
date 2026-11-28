package com.alexstudio.ai.matchstatistics.repository;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import com.alexstudio.ai.matchstatistics.entity.MatchStatistics;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface MatchStatisticsRepository extends JpaRepository<MatchStatistics, Long> {
    Optional<MatchStatistics> findByMatchId(Long matchId);
    List<MatchStatistics> findByMatchIdIn(Collection<Long> matchIds);
    boolean existsByMatchId(Long matchId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from MatchStatistics s where s.matchId = :matchId")
    Optional<MatchStatistics> findByMatchIdForUpdate(Long matchId);
}
