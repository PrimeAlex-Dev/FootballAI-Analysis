package com.alexstudio.ai.competitionseason.service;

import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.competition.service.CompetitionService;
import com.alexstudio.ai.competitionseason.dto.CompetitionSeasonRequest;
import com.alexstudio.ai.competitionseason.dto.CompetitionSeasonResponse;
import com.alexstudio.ai.competitionseason.entity.CompetitionSeason;
import com.alexstudio.ai.competitionseason.repository.CompetitionSeasonRepository;
import com.alexstudio.ai.season.service.SeasonService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetitionSeasonService {
    private final CompetitionSeasonRepository repository;
    private final CompetitionService competitions;
    private final SeasonService seasons;

    public CompetitionSeasonService(CompetitionSeasonRepository repository, CompetitionService competitions,
            SeasonService seasons) {
        this.repository = repository;
        this.competitions = competitions;
        this.seasons = seasons;
    }

    @Transactional
    public CompetitionSeasonResponse create(CompetitionSeasonRequest request) {
        var competition = competitions.find(request.competitionId());
        var season = seasons.find(request.seasonId());
        if (repository.existsByCompetition_IdAndSeason_Id(request.competitionId(), request.seasonId()))
            throw duplicate();
        try {
            return CompetitionSeasonResponse.from(repository.saveAndFlush(new CompetitionSeason(competition, season)));
        } catch (DataIntegrityViolationException exception) {
            throw duplicate();
        }
    }

    @Transactional(readOnly = true)
    public CompetitionSeasonResponse get(Long id) {
        return CompetitionSeasonResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public Page<CompetitionSeasonResponse> list(Long competitionId, Long seasonId, int page, int size) {
        Specification<CompetitionSeason> filter = (root, query, builder) -> builder.conjunction();
        if (competitionId != null)
            filter = filter
                    .and((root, query, builder) -> builder.equal(root.get("competition").get("id"), competitionId));
        if (seasonId != null)
            filter = filter.and((root, query, builder) -> builder.equal(root.get("season").get("id"), seasonId));
        return repository.findAll(filter, PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(CompetitionSeasonResponse::from);
    }

    @Transactional
    public void delete(Long id) {
        var value = find(id);
        try {
            repository.delete(value);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "Competition season has dependent data");
        }
    }

    private CompetitionSeason find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Competition season not found"));
    }

    private BusinessException duplicate() {
        return new BusinessException(HttpStatus.CONFLICT, "Competition season already exists");
    }
}
