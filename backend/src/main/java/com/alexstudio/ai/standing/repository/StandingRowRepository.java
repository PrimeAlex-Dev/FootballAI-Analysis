package com.alexstudio.ai.standing.repository;
import java.util.List;
import com.alexstudio.ai.standing.entity.StandingRow;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StandingRowRepository extends JpaRepository<StandingRow, Long> {
    List<StandingRow> findByStandingIdOrderByTableIndexAscRowIndexAsc(Long id);
    void deleteByStandingId(Long id);
}
