package com.alexstudio.ai.team.dto;
import com.alexstudio.ai.team.entity.TeamType;
public record TeamResponse(Long id, String name, TeamType type, String logo) {
}
