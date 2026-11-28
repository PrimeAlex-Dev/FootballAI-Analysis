export type Status =
  'SCHEDULED' | 'LIVE' | 'FINISHED' | 'POSTPONED' | 'CANCELLED'
export type Team = {
  id: number
  name: string
  logo: string | null
  type: 'CLUB' | 'NATIONAL'
}
export type Competition = {
  id: number
  name: string
  type: 'LEAGUE' | 'CUP'
  logo: string | null
}
export type Season = { id: number; name: string }
export type CompetitionSeason = {
  id: number
  competitionId: number
  seasonId: number
}
export type Match = {
  id: number
  competitionSeasonId: number
  homeTeamId: number
  awayTeamId: number
  kickoffTime: string
  status: Status
  homeScore: number | null
  awayScore: number | null
  round: string | null
  competitionSeasonName?: string
  homeTeamName?: string
  homeTeamLogo?: string | null
  awayTeamName?: string
  awayTeamLogo?: string | null
}
export type Prediction = {
  matchId: number
  home: number
  draw: number
  away: number
  confidence?: number
  summary: string
  factors: string[]
}
export type Statistic = {
  label: string
  home: number
  away: number
  unit?: string
}
export type RadarMetric = {
  name: string
  average: number | null
  availableMatches: number
  unit: string
}
export type RadarAxis = {
  key: string
  label: string
  score: number | null
  primary: RadarMetric
  supporting: RadarMetric[]
  referenceProfiles: number
}
export type RadarTeam = {
  teamId: number
  teamName: string
  historyMatches: number
  axes: RadarAxis[]
}
export type PlayingStyleRadar = {
  status: 'READY' | 'PARTIAL' | 'INSUFFICIENT_REFERENCE_DATA' | 'NOT_APPLICABLE'
  sampleLimit: number
  referenceMethod: string
  home: RadarTeam
  away: RadarTeam
}
export type Standing = {
  teamId: number
  rank?: number
  played: number
  won: number
  drawn: number
  lost: number
  goalsFor: number
  goalsAgainst: number
  goalDifference?: number
  points: number
}
