import { request } from '../shared/api'
export const statisticFields = [
  ['bigChances', 'Cơ hội rõ rệt'],
  ['clearances', 'Phá bóng giải nguy'],
  ['corners', 'Phạt góc'],
  ['crosses', 'Tạt bóng'],
  ['interceptions', 'Cắt bóng'],
  ['keyPasses', 'Đường chuyền tạo cơ hội'],
  ['longPasses', 'Chuyền dài'],
  ['passAccuracy', 'Chuyền chính xác (%)'],
  ['passesFinalThird', 'Chuyền ở 1/3 sân cuối'],
  ['possession', 'Kiểm soát bóng (%)'],
  ['redCards', 'Thẻ đỏ'],
  ['shots', 'Tổng cú sút'],
  ['shotsOnTarget', 'Sút trúng đích'],
  ['touchesInBox', 'Chạm bóng trong vòng cấm'],
  ['xg', 'Bàn thắng kỳ vọng (xG)'],
  ['yellowCards', 'Thẻ vàng'],
] as const
type StatisticKey = (typeof statisticFields)[number][0]
export type StatisticSide = Partial<Record<StatisticKey, number | null>>
export type StatisticsInput = { home: StatisticSide; away: StatisticSide }
export type StatisticsResponse = StatisticsInput & {
  matchId: number
  createdAt: string | null
  updatedAt: string | null
}
export const statisticsApi = {
  get: (matchId: number) =>
    request<StatisticsResponse>(`/matches/${matchId}/statistics`),
  preview: (matchId: number, input: StatisticsInput) =>
    request<StatisticsResponse>(`/matches/${matchId}/statistics/preview`, {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  create: (matchId: number, input: StatisticsInput) =>
    request<StatisticsResponse>(`/matches/${matchId}/statistics`, {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  update: (matchId: number, input: StatisticsInput) =>
    request<StatisticsResponse>(`/matches/${matchId}/statistics`, {
      method: 'PUT',
      body: JSON.stringify(input),
    }),
}
