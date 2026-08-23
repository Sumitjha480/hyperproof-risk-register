import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { CreateRiskPage } from './pages/CreateRiskPage'
import { DashboardPage } from './pages/DashboardPage'
import { EditRiskPage } from './pages/EditRiskPage'
import { RiskDetailPage } from './pages/RiskDetailPage'

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<DashboardPage />} />
        <Route path="risks/new" element={<CreateRiskPage />} />
        <Route path="risks/:riskId" element={<RiskDetailPage />} />
        <Route path="risks/:riskId/edit" element={<EditRiskPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
