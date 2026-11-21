package com.alexstudio.ai.competition.service;

import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.competition.dto.CompetitionRequest;
import com.alexstudio.ai.competition.dto.CompetitionResponse;
import com.alexstudio.ai.competition.entity.Competition;
import com.alexstudio.ai.competition.entity.CompetitionType;
import com.alexstudio.ai.competition.repository.CompetitionRepository;
import com.alexstudio.ai.competitionseason.repository.CompetitionSeasonRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetitionService {

    private final CompetitionRepository competitions;
    private final CompetitionSeasonRepository competitionSeasons;

    public CompetitionService(CompetitionRepository competitions, CompetitionSeasonRepository competitionSeasons) {
        this.competitions = competitions;
        this.competitionSeasons = competitionSeasons;
    }

    @Transactional
    public CompetitionResponse create(CompetitionRequest request) {
        Competition competition = new Competition(request.name().trim(), request.type(), normalizeLogo(request.logo()));
        return CompetitionResponse.from(competitions.save(competition));
    }

    @Transactional(readOnly = true)
    public CompetitionResponse get(Long id) {
        return CompetitionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public Page<CompetitionResponse> list(String name, CompetitionType type, int page, int size) {
        Specification<Competition> filter = (root, query, builder) -> builder.conjunction();
        if (name != null && !name.isBlank()) {
            String search = name.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            filter = filter.and(
                    (root, query, builder) -> builder.like(builder.lower(root.get("name")), "%" + search + "%", '\\'));
        }
        if (type != null) {
            filter = filter.and((root, query, builder) -> builder.equal(root.get("type"), type));
        }
        return competitions.findAll(filter, PageRequest.of(page, size, Sort.by("name").ascending().and(Sort.by("id"))))
                .map(CompetitionResponse::from);
    }

    @Transactional
    public CompetitionResponse update(Long id, CompetitionRequest request) {
        Competition competition = find(id);
        competition.update(request.name().trim(), request.type(), normalizeLogo(request.logo()));
        return CompetitionResponse.from(competition);
    }

    @Transactional
    public void delete(Long id) {
        Competition competition = find(id);
        if (competitionSeasons.existsByCompetition_Id(id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Competition is in use");
        }
        try {
            competitions.delete(competition);
            competitions.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "Competition has dependent data");
        }
    }

    public Competition find(Long id) {
        return competitions.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Competition not found"));
    }

    private String normalizeLogo(String logo) {
        return logo == null ? null : logo.trim();
    }
}
