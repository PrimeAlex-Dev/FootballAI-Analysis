package com.alexstudio.ai.team.service;

import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.team.dto.TeamRequest;
import com.alexstudio.ai.team.dto.TeamResponse;
import com.alexstudio.ai.team.entity.Team;
import com.alexstudio.ai.team.entity.TeamType;
import com.alexstudio.ai.team.repository.TeamRepository;
import com.alexstudio.ai.team.mapper.TeamMapper;
import java.util.Optional;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    private final TeamRepository teams;

    public TeamService(TeamRepository teams) {
        this.teams = teams;

    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        String name = request.name().trim();
        if (teams.existsByNameIgnoreCase(name)) {
            throw duplicate();
        }
        Team team = new Team(name, request.type(), normalizeLogo(request.logo()));
        try {
            return TeamMapper.toResponse(teams.saveAndFlush(team));
        } catch (DataIntegrityViolationException exception) {
            throw duplicate();
        }
    }

    @Transactional(readOnly = true)
    public TeamResponse get(Long id) {
        return TeamMapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Optional<TeamResponse> findByExactName(String name) {
        return teams.findByNameIgnoreCase(name.trim()).map(TeamMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<Long, TeamResponse> getByIds(Collection<Long> ids) {
        return teams.findAllById(ids).stream().map(TeamMapper::toResponse)
                .collect(Collectors.toMap(TeamResponse::id, value -> value));
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> list(String name, TeamType type, int page, int size) {
        Specification<Team> filter = (root, query, builder) -> builder.conjunction();
        if (name != null && !name.isBlank()) {
            String search = name.trim().toLowerCase(java.util.Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%")
                    .replace("_", "\\_");
            filter = filter.and(
                    (root, query, builder) -> builder.like(builder.lower(root.get("name")), "%" + search + "%", '\\'));
        }
        if (type != null) {
            filter = filter.and((root, query, builder) -> builder.equal(root.get("type"), type));
        }
        return teams.findAll(filter, PageRequest.of(page, size, Sort.by("name").ascending().and(Sort.by("id"))))
                .map(TeamMapper::toResponse);
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = find(id);
        String name = request.name().trim();
        if (teams.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicate();
        }
        team.update(name, request.type(), normalizeLogo(request.logo()));
        try {
            teams.flush();
        } catch (DataIntegrityViolationException exception) {
            throw duplicate();
        }
        return TeamMapper.toResponse(team);
    }

    @Transactional
    public void delete(Long id) {
        Team team = find(id);

        try {
            teams.delete(team);
            teams.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "Team has dependent data");
        }
    }

    public Team find(Long id) {
        return teams.findById(id).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    private BusinessException duplicate() {
        return new BusinessException(HttpStatus.CONFLICT, "Team name already exists");
    }

    private String normalizeLogo(String logo) {
        return logo == null ? null : logo.trim();
    }
}
