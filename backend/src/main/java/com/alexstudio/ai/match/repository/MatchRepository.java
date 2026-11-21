package com.alexstudio.ai.match.repository;
import java.time.Instant;
import java.util.Optional;
import com.alexstudio.ai.match.entity.Match;
import com.alexstudio.ai.match.entity.MatchStatus;
import com.alexstudio.ai.match.dto.MatchResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
public interface MatchRepository extends JpaRepository<Match, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Match m where m.id = :id")
    Optional<Match> findForUpdate(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Match m set m.status = com.alexstudio.ai.match.entity.MatchStatus.LIVE where m.status = com.alexstudio.ai.match.entity.MatchStatus.SCHEDULED and m.kickoffTime <= :now")
    int promoteDue(Instant now);

    String PROJECTION = """
            select new com.alexstudio.ai.match.dto.MatchResponse(
                m.id, m.competitionSeasonId, m.homeTeamId, m.awayTeamId, m.kickoffTime, m.status,
                m.homeScore, m.awayScore, m.round, concat(c.name, ' ', s.name), h.name, h.logo, a.name, a.logo)
            from Match m, CompetitionSeason cs, Competition c, Season s, Team h, Team a
            where m.competitionSeasonId = cs.id and cs.competition.id = c.id and cs.season.id = s.id
                and m.homeTeamId = h.id and m.awayTeamId = a.id
            """;
    @Query(PROJECTION + " and m.id = :id")
    Optional<MatchResponse> findResponse(Long id);

    @Query(PROJECTION + """
            and (:name is null or lower(h.name) like :name escape '\\' or lower(a.name) like :name escape '\\')
            and (:competitionSeasonId is null or m.competitionSeasonId = :competitionSeasonId)
            and (:homeTeamId is null or m.homeTeamId = :homeTeamId)
            and (:awayTeamId is null or m.awayTeamId = :awayTeamId)
            and (:teamId is null or m.homeTeamId = :teamId or m.awayTeamId = :teamId)
            and (:status is null or m.status = :status)
            and (cast(:fromTime as Instant) is null or m.kickoffTime >= :fromTime)
            and (cast(:toTime as Instant) is null or m.kickoffTime <= :toTime)
            """)
    Page<MatchResponse> search(String name, Long competitionSeasonId, Long homeTeamId, Long awayTeamId, Long teamId,
            MatchStatus status, Instant fromTime, Instant toTime, Pageable pageable);
}
