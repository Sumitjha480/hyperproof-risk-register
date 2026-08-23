import { Link, Outlet, useLocation } from 'react-router-dom'

export function AppShell() {
  const location = useLocation()
  const onDashboard = location.pathname === '/'

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-inner">
          <Link className="brand" to="/" aria-label="Risk Register dashboard">
            <span className="brand-mark" aria-hidden="true">R</span>
            <span>
              <strong>Risk Register</strong>
              <small>Prioritize, mitigate, verify</small>
            </span>
          </Link>
          {!onDashboard && (
            <Link className="button button-secondary button-small" to="/">
              Back to dashboard
            </Link>
          )}
        </div>
      </header>
      <main className="page-container">
        <Outlet />
      </main>
      <footer className="footer">
        Scores are derived from likelihood, impact, and recorded mitigations.
      </footer>
    </div>
  )
}
