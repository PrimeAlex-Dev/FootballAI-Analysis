import { request, type Page } from '../shared/api'
export interface Season {
  id: number
  name: string
}
export const seasonApi = {
  list: (params: string, signal?: AbortSignal) =>
    request<Page<Season>>(`/seasons?${params}`, { signal }),
  get: (id: number) => request<Season>(`/seasons/${id}`),
  create: (value: { name: string }) =>
    request<Season>('/seasons', {
      method: 'POST',
      body: JSON.stringify(value),
    }),
  update: (id: number, value: { name: string }) =>
    request<Season>(`/seasons/${id}`, {
      method: 'PUT',
      body: JSON.stringify(value),
    }),
  remove: (id: number) => request<void>(`/seasons/${id}`, { method: 'DELETE' }),
}
