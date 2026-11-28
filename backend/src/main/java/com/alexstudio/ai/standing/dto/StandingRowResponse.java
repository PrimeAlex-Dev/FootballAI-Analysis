package com.alexstudio.ai.standing.dto;
public record StandingRowResponse(int tableIndex, int rowIndex, Long teamId, String teamName, String teamLogo,
        String sourceTeam, int rank, int matchesPlayed, int wins, int draws, int losses, int goalsFor, int goalsAgainst,
        int goalDifference, int points) {
}
