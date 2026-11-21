import { request, type Page } from '../shared/api'
export interface CompetitionSeason {
  id: number
  competitionId: number
  seasonId: number
  displayName: string
}
export const competitionSeasonApi = {
  list: (params: string, signal?: AbortSignal) =>
    request<Page<CompetitionSeason>>(`/competition-seasons?${params}`, {
      signal,
    }),
  get: (id: number) => request<CompetitionSeason>(`/competition-seasons/${id}`),
  create: (value: { competitionId: number; seasonId: number }) =>
    request<CompetitionSeason>('/competition-seasons', {
      method: 'POST',
      body: JSON.stringify(value),
    }),
  remove: (id: number) =>
    request<void>(`/competition-seasons/${id}`, { method: 'DELETE' }),
}
