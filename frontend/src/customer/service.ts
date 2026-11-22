import {
  competitionSeasons,
  competitions,
  matches,
  predictions,
  seasons,
  standings,
  statistics,
  teams,
} from './mock'
import type { Match, Status } from './types'

export const team = (id: number) => teams.find((item) => item.id === id)!
export const competitionSeason = (id: number) => {
  const link = competitionSeasons.find((item) => item.id === id)!
  return {
    ...link,
    competition: competitions.find((item) => item.id === link.competitionId)!,
    season: seasons.find((item) => item.id === link.seasonId)!,
  }
}
export const dateKey = (iso: string) =>
  new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Ho_Chi_Minh',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(iso))
export const dateLabel = (iso: string) =>
  new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Ho_Chi_Minh',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(iso))
export const timeLabel = (iso: string) =>
  new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Ho_Chi_Minh',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(iso))
export const score = (match: Match) =>
  match.homeScore === null ||
  match.awayScore === null ||
  ['SCHEDULED', 'POSTPONED', 'CANCELLED'].includes(match.status)
    ? '- : -'
    : `${match.homeScore} : ${match.awayScore}`
export const statusLabel: Record<Status, string> = {
  SCHEDULED: 'Sắp diễn ra',
  LIVE: 'Đang diễn ra',
  FINISHED: 'Đã kết thúc',
  POSTPONED: 'Đã hoãn',
  CANCELLED: 'Đã hủy',
}
export type MatchFilters = {
  date: string
  search: string
  competitionSeasonId: number | null
  status: Status | null
}
export async function getMatches(filters: MatchFilters) {
  const text = filters.search.trim().toLocaleLowerCase('vi-VN')
  return matches
    .filter(
      (match) =>
        dateKey(match.kickoffTime) === filters.date &&
        (!text ||
          team(match.homeTeamId)
            .name.toLocaleLowerCase('vi-VN')
            .includes(text) ||
          team(match.awayTeamId)
            .name.toLocaleLowerCase('vi-VN')
            .includes(text)) &&
        (!filters.competitionSeasonId ||
          match.competitionSeasonId === filters.competitionSeasonId) &&
        (!filters.status || match.status === filters.status),
    )
    .sort((a, b) => a.kickoffTime.localeCompare(b.kickoffTime))
}
export async function getMatchById(id: number) {
  return matches.find((match) => match.id === id) ?? null
}
export async function getMatchPrediction(id: number) {
  return predictions.find((item) => item.matchId === id) ?? null
}
export async function getMatchStatistics(id: number) {
  return statistics[id] ?? null
}
export async function getStandings(id: number) {
  return standings[id] ?? null
}
export type FormResult = { match: Match; outcome: 'W' | 'D' | 'L' }
export async function getRecentForm(
  teamId: number,
  beforeTime: string,
  limit = 10,
): Promise<FormResult[]> {
  return matches
    .filter(
      (match) =>
        match.status === 'FINISHED' &&
        match.kickoffTime < beforeTime &&
        (match.homeTeamId === teamId || match.awayTeamId === teamId),
    )
    .sort((a, b) => b.kickoffTime.localeCompare(a.kickoffTime))
    .slice(0, limit)
    .map((match) => {
      const own =
        match.homeTeamId === teamId ? match.homeScore! : match.awayScore!
      const other =
        match.homeTeamId === teamId ? match.awayScore! : match.homeScore!
      return { match, outcome: own > other ? 'W' : own < other ? 'L' : 'D' }
    })
}
export async function getHeadToHead(
  homeId: number,
  awayId: number,
  beforeTime: string,
) {
  return matches
    .filter(
      (match) =>
        match.status === 'FINISHED' &&
        match.kickoffTime < beforeTime &&
        ((match.homeTeamId === homeId && match.awayTeamId === awayId) ||
          (match.homeTeamId === awayId && match.awayTeamId === homeId)),
    )
    .sort((a, b) => b.kickoffTime.localeCompare(a.kickoffTime))
}
