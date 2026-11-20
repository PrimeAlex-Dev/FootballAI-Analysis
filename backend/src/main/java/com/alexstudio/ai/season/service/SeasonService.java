package com.alexstudio.ai.season.service;

import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.competition.entity.Competition;
import com.alexstudio.ai.competition.service.CompetitionService;
import com.alexstudio.ai.season.dto.SeasonRequest;
import com.alexstudio.ai.season.dto.SeasonResponse;
import com.alexstudio.ai.season.entity.Season;
import com.alexstudio.ai.season.repository.SeasonRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeasonService {

    private final SeasonRepository seasons;
    private final CompetitionService competitions;

    public SeasonService(SeasonRepository seasons, CompetitionService competitions) {
        this.seasons = seasons;
        this.competitions = competitions;
    }

    @Transactional
    public SeasonResponse create(Long competitionId, SeasonRequest request) {
        Competition competition = competitions.find(competitionId);
        String name = validName(request.name());
        if (seasons.existsByCompetition_IdAndName(competitionId, name)) {
            throw duplicate();
        }
        try {
            return SeasonResponse.from(seasons.saveAndFlush(new Season(competition, name)));
        } catch (DataIntegrityViolationException exception) {
            throw duplicate();
        }
    }

    @Transactional(readOnly = true)
    public SeasonResponse get(Long competitionId, Long id) {
        return SeasonResponse.from(find(competitionId, id));
    }

    @Transactional(readOnly = true)
    public Page<SeasonResponse> list(Long competitionId, int page, int size) {
        competitions.find(competitionId);
        return seasons
                .findByCompetition_Id(competitionId,
                        PageRequest.of(page, size, Sort.by("name").descending().and(Sort.by("id").descending())))
                .map(SeasonResponse::from);
    }

    @Transactional
    public SeasonResponse update(Long competitionId, Long id, SeasonRequest request) {
        Season season = find(competitionId, id);
        String name = validName(request.name());
        if (!season.getName().equals(name) && seasons.existsByCompetition_IdAndName(competitionId, name)) {
            throw duplicate();
        }
        season.rename(name);
        try {
            seasons.flush();
        } catch (DataIntegrityViolationException exception) {
            throw duplicate();
        }
        return SeasonResponse.from(season);
    }

    @Transactional
    public void delete(Long competitionId, Long id) {
        Season season = find(competitionId, id);
        try {
            seasons.delete(season);
            seasons.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "Season has dependent data");
        }
    }

    private Season find(Long competitionId, Long id) {
        return seasons.findByIdAndCompetition_Id(id, competitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Season not found"));
    }

    private String validName(String value) {
        String name = value.trim();
        if (!name.matches("[0-9]{4}(/[0-9]{4})?")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Season name must be YYYY or YYYY/YYYY");
        }
        if (name.length() == 9 && Integer.parseInt(name.substring(5)) != Integer.parseInt(name.substring(0, 4)) + 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Season years must be consecutive");
        }
        return name;
    }

    private BusinessException duplicate() {
        return new BusinessException(HttpStatus.CONFLICT, "Season name already exists in this competition");
    }
}
