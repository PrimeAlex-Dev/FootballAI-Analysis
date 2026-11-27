import { request, type Page } from '../shared/api'
export type MatchStatus =
  'SCHEDULED' | 'LIVE' | 'FINISHED' | 'POSTPONED' | 'CANCELLED'
export interface MatchInput {
  competitionSeasonId: number
  homeTeamId: number
  awayTeamId: number
  kickoffTime: string
  status: MatchStatus
  homeScore: number | null
  awayScore: number | null
  round: string | null
}
export interface Match extends MatchInput {
  id: number
  competitionSeasonName: string
  homeTeamName: string
  homeTeamLogo: string | null
  awayTeamName: string
  awayTeamLogo: string | null
}
export type FormMatch = { match: Match; outcome: 'W' | 'D' | 'L' }
export type RecentForm = {
  homeTeamId: number
  awayTeamId: number
  home: FormMatch[]
  away: FormMatch[]
}
export type HeadToHead = {
  homeTeamId: number
  awayTeamId: number
  homeWins: number
  draws: number
  awayWins: number
  matches: Page<Match>
}
export const matchApi = {
  list: (params: string, signal?: AbortSignal) =>
    request<Page<Match>>(`/matches?${params}`, { signal }),
  get: (id: number) => request<Match>(`/matches/${id}`),
  recentForm: (id: number) => request<RecentForm>(`/matches/${id}/recent-form`),
  headToHead: (id: number, page: number) =>
    request<HeadToHead>(`/matches/${id}/head-to-head?page=${page}&size=5`),
  create: (input: MatchInput) =>
    request<Match>('/matches', { method: 'POST', body: JSON.stringify(input) }),
  update: (id: number, input: MatchInput) =>
    request<Match>(`/matches/${id}`, {
      method: 'PUT',
      body: JSON.stringify(input),
    }),
  remove: (id: number) => request<void>(`/matches/${id}`, { method: 'DELETE' }),
}
export const statusLabels: Record<MatchStatus, string> = {
  SCHEDULED: 'Chưa diễn ra',
  LIVE: 'LIVE · Đang diễn ra',
  FINISHED: 'Đã kết thúc',
  POSTPONED: 'Bị hoãn',
  CANCELLED: 'Bị hủy',
}
export function formatScore(value: Pick<Match, 'homeScore' | 'awayScore'>) {
  return value.homeScore === null || value.awayScore === null
    ? '- : -'
    : `${value.homeScore} : ${value.awayScore}`
}
export const MATCH_TIME_ZONE = 'Asia/Ho_Chi_Minh'
export function toVietnamInput(instant: string) {
  const date = new Date(instant)
  if (Number.isNaN(date.getTime())) return ''
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: MATCH_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(date)
  const part = (type: string) =>
    parts.find((value) => value.type === type)?.value || ''
  return (
    part('year') +
    '-' +
    part('month') +
    '-' +
    part('day') +
    'T' +
    part('hour') +
    ':' +
    part('minute') +
    ':' +
    part('second')
  )
}
export function toVietnamIso(value: string) {
  if (!value) return ''
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d{1,3})?)?$/.test(value))
    return ''
  const local = value.length === 16 ? value + ':00' : value
  const iso = local + '+07:00'
  return toVietnamInput(iso) === local.slice(0, 19) ? iso : ''
}
export function formatVietnamTime(instant: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: MATCH_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23',
  }).format(new Date(instant))
}
