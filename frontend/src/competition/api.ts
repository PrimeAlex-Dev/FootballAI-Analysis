import { request, type Page } from '../shared/api'
export interface Competition {
  id: number
  name: string
  type: 'LEAGUE' | 'CUP'
  logo: string | null
}
export type CompetitionInput = Omit<Competition, 'id'>
export const competitionApi = {
  list: (params: string, signal?: AbortSignal) =>
    request<Page<Competition>>(`/competitions?${params}`, { signal }),
  get: (id: number) => request<Competition>(`/competitions/${id}`),
  create: (value: CompetitionInput) =>
    request<Competition>('/competitions', {
      method: 'POST',
      body: JSON.stringify(value),
    }),
  update: (id: number, value: CompetitionInput) =>
    request<Competition>(`/competitions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(value),
    }),
  remove: (id: number) =>
    request<void>(`/competitions/${id}`, { method: 'DELETE' }),
}
