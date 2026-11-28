package com.alexstudio.ai.standing.repository;
import java.util.Optional;
import com.alexstudio.ai.standing.entity.Standing;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
public interface StandingRepository extends JpaRepository<Standing, Long> {
    Optional<Standing> findByCompetitionSeasonId(Long id);
    Optional<Standing> findByCompetitionSeasonIdAndHiddenFalse(Long id);
    Page<Standing> findPageByCompetitionSeasonId(Long id, Pageable pageable);
    Page<Standing> findByHidden(boolean hidden, Pageable pageable);
    Page<Standing> findByCompetitionSeasonIdAndHidden(Long id, boolean hidden, Pageable pageable);
    boolean existsByCompetitionSeasonId(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Standing s where s.competitionSeasonId = :id")
    Optional<Standing> lockByCompetitionSeasonId(Long id);
}
