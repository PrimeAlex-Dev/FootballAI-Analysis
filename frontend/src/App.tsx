import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import TemplateUi from './template-ui/TemplateUi'
import DashboardLayout, { DashboardHome } from './dashboard/DashboardLayout'
import CompetitionsPage from './competition/CompetitionsPage'
import SeasonsPage from './season/SeasonsPage'
import CompetitionSeasonsPage from './competition-season/CompetitionSeasonsPage'
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/template-ui" element={<TemplateUi />} />
        <Route path="/dashboard" element={<DashboardLayout />}>
          <Route index element={<DashboardHome />} />
          <Route path="competitions" element={<CompetitionsPage />} />
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
