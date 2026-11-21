import { request, type Page } from '../shared/api'
export interface Team {
  id: number
  name: string
  type: 'NATIONAL' | 'CLUB'
  logo: string | null
}
export type TeamInput = Omit<Team, 'id'>
export const teamApi = {
  list: (params: string, signal?: AbortSignal) =>
    request<Page<Team>>(`/teams?${params}`, { signal }),
  get: (id: number) => request<Team>(`/teams/${id}`),
  create: (value: TeamInput) =>
    request<Team>('/teams', {
      method: 'POST',
      body: JSON.stringify(value),
    }),
  update: (id: number, value: TeamInput) =>
    request<Team>(`/teams/${id}`, {
      method: 'PUT',
      body: JSON.stringify(value),
    }),
  remove: (id: number) => request<void>(`/teams/${id}`, { method: 'DELETE' }),
}
