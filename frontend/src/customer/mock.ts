import type {
  Competition,
  CompetitionSeason,
  Match,
  Prediction,
  Season,
  Standing,
  Statistic,
  Team,
} from './types'

export const teams: Team[] = [
  { id: 1, name: 'Inter', logo: null, type: 'CLUB' },
  { id: 2, name: 'Fiorentina', logo: null, type: 'CLUB' },
  { id: 3, name: 'AC Milan', logo: null, type: 'CLUB' },
  { id: 4, name: 'Napoli', logo: null, type: 'CLUB' },
  { id: 5, name: 'Liverpool', logo: null, type: 'CLUB' },
  { id: 6, name: 'Arsenal', logo: null, type: 'CLUB' },
  { id: 7, name: 'Manchester City', logo: null, type: 'CLUB' },
  { id: 8, name: 'Barcelona', logo: null, type: 'CLUB' },
  { id: 9, name: 'Real Madrid', logo: null, type: 'CLUB' },
  { id: 10, name: 'Sevilla', logo: null, type: 'CLUB' },
  { id: 11, name: 'Juventus', logo: null, type: 'CLUB' },
  { id: 12, name: 'Roma', logo: null, type: 'CLUB' },
]
export const competitions: Competition[] = [
  { id: 1, name: 'Serie A', type: 'LEAGUE', logo: null },
  { id: 2, name: 'Premier League', type: 'LEAGUE', logo: null },
  { id: 3, name: 'La Liga', type: 'LEAGUE', logo: null },
  { id: 4, name: 'Coppa Italia', type: 'CUP', logo: null },
]
export const seasons: Season[] = [{ id: 1, name: '2026/2027' }]
export const competitionSeasons: CompetitionSeason[] = [
  { id: 101, competitionId: 1, seasonId: 1 },
  { id: 102, competitionId: 2, seasonId: 1 },
  { id: 103, competitionId: 3, seasonId: 1 },
  { id: 104, competitionId: 4, seasonId: 1 },
]
export const todayKey = new Intl.DateTimeFormat('en-CA', {
  timeZone: 'Asia/Ho_Chi_Minh',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
}).format(new Date())
const day = (offset: number, hour: number, minute = 0) => {
  const [year, month, date] = todayKey.split('-').map(Number)
  return new Date(
    Date.UTC(year, month - 1, date + offset, hour - 7, minute),
  ).toISOString()
}
const m = (
  id: number,
  cs: number,
  home: number,
  away: number,
  offset: number,
  hour: number,
  status: Match['status'],
  hs: number | null,
  as: number | null,
  round: string | null,
): Match => ({
  id,
  competitionSeasonId: cs,
  homeTeamId: home,
  awayTeamId: away,
  kickoffTime: day(offset, hour),
  status,
  homeScore: hs,
  awayScore: as,
  round,
})
export const matches: Match[] = [
  m(1, 101, 1, 2, 0, 18, 'FINISHED', 3, 0, 'Vòng 9'),
  m(2, 101, 3, 4, 0, 21, 'SCHEDULED', null, null, 'Vòng 9'),
  m(3, 102, 5, 6, 0, 23, 'LIVE', null, null, 'Vòng 12'),
  m(4, 102, 7, 5, 0, 16, 'LIVE', 1, 0, 'Vòng 12'),
  m(5, 103, 8, 10, 0, 20, 'SCHEDULED', null, null, 'Vòng 11'),
  m(6, 103, 9, 8, 0, 22, 'POSTPONED', null, null, null),
  m(7, 101, 11, 12, 1, 20, 'SCHEDULED', null, null, 'Vòng 10'),
  m(8, 102, 6, 7, 1, 21, 'SCHEDULED', null, null, 'Vòng 13'),
  m(9, 104, 1, 3, 1, 18, 'CANCELLED', null, null, null),
  m(10, 103, 9, 10, -1, 20, 'FINISHED', 0, 0, 'Vòng 10'),
  m(11, 101, 2, 1, -1, 18, 'FINISHED', 1, 2, 'Vòng 8'),
  m(12, 101, 1, 4, -3, 20, 'FINISHED', 2, 1, 'Vòng 7'),
  m(13, 101, 3, 1, -6, 21, 'FINISHED', 0, 1, 'Vòng 6'),
  m(14, 101, 1, 2, -11, 19, 'FINISHED', 2, 2, 'Vòng 5'),
  m(15, 101, 2, 4, -15, 20, 'FINISHED', 2, 0, 'Vòng 4'),
  m(16, 101, 1, 11, -18, 20, 'FINISHED', 3, 1, 'Vòng 3'),
  m(17, 101, 2, 1, -28, 20, 'FINISHED', 0, 0, 'Vòng 2'),
  m(18, 101, 1, 12, -35, 18, 'FINISHED', 1, 0, 'Vòng 1'),
  m(19, 101, 1, 2, -70, 20, 'FINISHED', 1, 0, null),
  m(20, 101, 2, 1, -110, 20, 'FINISHED', 3, 0, null),
  m(21, 101, 1, 2, -150, 19, 'FINISHED', 0, 0, null),
  m(22, 101, 1, 2, -190, 20, 'FINISHED', 2, 1, null),
  m(23, 101, 2, 1, -230, 20, 'FINISHED', 1, 1, null),
]
export const predictions: Prediction[] = [
  {
    matchId: 1,
    home: 60,
    draw: 25,
    away: 15,
    confidence: 86,
    summary:
      'Inter có lợi thế sân nhà và kết quả tích cực trong những lần gặp gần đây. Fiorentina vẫn có cơ hội nếu kiểm soát được tuyến giữa.',
    factors: ['Phong độ gần đây', 'Lợi thế sân nhà', 'Lịch sử đối đầu'],
  },
  {
    matchId: 3,
    home: 42,
    draw: 30,
    away: 28,
    summary: 'Hai đội có tương quan khá cân bằng trong bộ dữ liệu minh họa.',
    factors: ['Phong độ gần đây', 'Hiệu suất ghi bàn'],
  },
]
export const statistics: Record<number, Statistic[]> = {
  1: [
    { label: 'Kiểm soát bóng', home: 58, away: 42, unit: '%' },
    { label: 'Số cú sút', home: 16, away: 8 },
    { label: 'Sút trúng đích', home: 7, away: 3 },
    { label: 'Phạt góc', home: 4, away: 2 },
    { label: 'Thẻ vàng', home: 1, away: 3 },
    { label: 'Thẻ đỏ', home: 0, away: 0 },
  ],
}
export const standings: Record<number, Standing[]> = {
  101: [
    {
      teamId: 1,
      played: 9,
      won: 8,
      drawn: 0,
      lost: 1,
      goalsFor: 22,
      goalsAgainst: 8,
      points: 24,
    },
    {
      teamId: 4,
      played: 9,
      won: 7,
      drawn: 0,
      lost: 2,
      goalsFor: 19,
      goalsAgainst: 9,
      points: 21,
    },
    {
      teamId: 3,
      played: 9,
      won: 6,
      drawn: 1,
      lost: 2,
      goalsFor: 18,
      goalsAgainst: 10,
      points: 19,
    },
    {
      teamId: 11,
      played: 9,
      won: 5,
      drawn: 2,
      lost: 2,
      goalsFor: 17,
      goalsAgainst: 11,
      points: 17,
    },
    {
      teamId: 2,
      played: 9,
      won: 4,
      drawn: 3,
      lost: 2,
      goalsFor: 14,
      goalsAgainst: 11,
      points: 15,
    },
    {
      teamId: 12,
      played: 9,
      won: 4,
      drawn: 2,
      lost: 3,
      goalsFor: 13,
      goalsAgainst: 11,
      points: 14,
    },
  ],
}
