import { request, type Page } from '../shared/api'
export type StandingRow = {
  tableIndex: number
  rowIndex: number
  teamId: number | null
  teamName: string | null
  teamLogo: string | null
  sourceTeam: string
  rank: number
  matchesPlayed: number
  wins: number
  draws: number
  losses: number
  goalsFor: number
  goalsAgainst: number
  goalDifference: number
  points: number
}
export type StandingPreview = {
  competitionSeasonId: number
  competitionSeasonName: string
  tables: StandingRow[][]
}
export type Standing = StandingPreview & {
  id: number
  createdAt: string
  updatedAt: string
}
export type StandingSummary = {
  id: number
  competitionSeasonId: number
  competitionSeasonName: string
  tableCount: number
  teamCount: number
  updatedAt: string
}
export type TeamMapping = {
  tableIndex: number
  rowIndex: number
  teamId: number
}
export type StandingInput = { tables: unknown; mappings?: TeamMapping[] }
export const standingApi = {
  list: (params: string, signal?: AbortSignal) =>
    request<Page<StandingSummary>>(`/standings?${params}`, { signal }),
  get: (id: number) => request<Standing>(`/standings/${id}`),
  getByCompetitionSeason: (id: number) =>
    request<Standing>(`/competition-seasons/${id}/standing`),
  preview: (id: number, input: StandingInput) =>
    request<StandingPreview>(`/competition-seasons/${id}/standing/preview`, {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  create: (id: number, input: StandingInput) =>
    request<Standing>(`/competition-seasons/${id}/standing`, {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  update: (id: number, input: StandingInput) =>
    request<Standing>(`/competition-seasons/${id}/standing`, {
      method: 'PUT',
      body: JSON.stringify(input),
    }),
  remove: (id: number) =>
    request<void>(`/competition-seasons/${id}/standing`, { method: 'DELETE' }),
}
