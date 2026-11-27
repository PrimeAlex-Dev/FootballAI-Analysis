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
import { matchApi } from '../match/api'
import { competitionSeasonApi } from '../competition-season/api'
import { query } from '../shared/api'

const realTeams = new Map<number, { name: string; logo: string | null }>()
const realLinks = new Map<number, string>()
const realMatches = new Set<number>()
export const team = (id: number) =>
  realTeams.get(id) ??
  teams.find((item) => item.id === id) ?? {
    name: 'Chưa có dữ liệu',
    logo: null,
  }
export const competitionSeason = (id: number) => {
  const real = realLinks.get(id)
  if (real) return { competition: { name: real }, season: { name: '' } }
  const link = competitionSeasons.find((item) => item.id === id)
  return {
    competition: competitions.find(
      (item) => item.id === link?.competitionId,
    ) ?? { name: 'Chưa có dữ liệu' },
    season: seasons.find((item) => item.id === link?.seasonId) ?? { name: '' },
  }
}
function register(match: Match) {
  realMatches.add(match.id)
  realTeams.set(match.homeTeamId, {
    name: match.homeTeamName ?? 'Chưa có dữ liệu',
    logo: match.homeTeamLogo ?? null,
  })
  realTeams.set(match.awayTeamId, {
    name: match.awayTeamName ?? 'Chưa có dữ liệu',
    logo: match.awayTeamLogo ?? null,
  })
  realLinks.set(
    match.competitionSeasonId,
    match.competitionSeasonName ?? 'Chưa có dữ liệu',
  )
  return match
}
export async function getCompetitionSeasons() {
  const items: { id: number; displayName: string }[] = []
  for (let page = 0; ; page++) {
    const result = await competitionSeasonApi.list(query({ page, size: 100 }))
    items.push(...result.content)
    if (page + 1 >= result.page.totalPages) return items
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
  const items: Match[] = []
  for (let page = 0; ; page++) {
    const result = await matchApi.list(
      query({
        from: `${filters.date}T00:00:00+07:00`,
        to: `${filters.date}T23:59:59.999+07:00`,
        name: filters.search.trim(),
        competitionSeasonId: filters.competitionSeasonId ?? '',
        status: filters.status ?? '',
        direction: 'ASC',
        page,
        size: 100,
      }),
    )
    items.push(...result.content.map(register))
    if (page + 1 >= result.page.totalPages) return items
  }
}
export async function getMatchById(id: number) {
  if (!Number.isSafeInteger(id) || id < 1) return null
  try {
    return register(await matchApi.get(id))
  } catch (error) {
    if (
      error instanceof Error &&
      error.message === 'Trận đấu không còn tồn tại.'
    )
      return null
    throw error
  }
}
export async function getMatchPrediction(id: number) {
  return realMatches.has(id)
    ? null
    : (predictions.find((item) => item.matchId === id) ?? null)
}
export async function getMatchStatistics(id: number) {
  return realMatches.has(id) ? null : (statistics[id] ?? null)
}
export async function getStandings(id: number) {
  return realLinks.has(id) ? null : (standings[id] ?? null)
}
export type FormResult = { match: Match; outcome: 'W' | 'D' | 'L' }
export async function getRecentForm(
  teamId: number,
  beforeTime: string,
  limit = 10,
): Promise<FormResult[]> {
  if (realTeams.has(teamId)) return []
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
  if (realTeams.has(homeId) || realTeams.has(awayId)) return []
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
