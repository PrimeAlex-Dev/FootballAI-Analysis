package com.alexstudio.ai.team.mapper;
import com.alexstudio.ai.team.dto.TeamResponse;
import com.alexstudio.ai.team.entity.Team;
public final class TeamMapper {
    private TeamMapper() {
    }
    public static TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getType(), team.getLogo());
    }
}
