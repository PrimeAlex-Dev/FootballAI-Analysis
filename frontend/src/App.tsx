import MatchesPage from './match/MatchesPage'
import TeamsPage from './team/TeamsPage'
import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import TemplateUi from './template-ui/TemplateUi'
import DashboardLayout, { DashboardHome } from './dashboard/DashboardLayout'
import CompetitionsPage from './competition/CompetitionsPage'
import SeasonsPage from './season/SeasonsPage'
import CompetitionSeasonsPage from './competition-season/CompetitionSeasonsPage'
import {
  AiPage,
  CustomerLayout,
  DetailIndex,
  FormPage,
  MatchDetailLayout,
  MatchOverviewPage,
  StandingsPage,
  StatisticsPage,
} from './customer/CustomerPages'
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/template-ui" element={<TemplateUi />} />
        <Route element={<CustomerLayout />}>
          <Route path="/matches" element={<MatchOverviewPage />} />
          <Route path="/matches/:matchId" element={<MatchDetailLayout />}>
            <Route index element={<DetailIndex />} />
            <Route path="ai" element={<AiPage />} />
            <Route path="form" element={<FormPage />} />
            <Route path="statistics" element={<StatisticsPage />} />
            <Route path="standings" element={<StandingsPage />} />
          </Route>
        </Route>
        <Route path="/dashboard" element={<DashboardLayout />}>
          <Route index element={<DashboardHome />} />
          <Route path="competitions" element={<CompetitionsPage />} />
          <Route path="matches" element={<MatchesPage />} />
          <Route path="teams" element={<TeamsPage />} />
          <Route path="seasons" element={<SeasonsPage />} />
          <Route
            path="competition-seasons"
            element={<CompetitionSeasonsPage />}
          />
        </Route>
        <Route
          path="*"
          element={
            <main className="welcome">
              <h1>Không tìm thấy trang</h1>
              <Link to="/dashboard">Về dashboard</Link>
            </main>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}
