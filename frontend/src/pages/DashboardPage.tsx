import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { riskApi } from '../api/client'
import { PageMessage } from '../components/PageMessage'
import { RiskTable } from '../components/RiskTable'
import {
  RISK_CATEGORIES,
  RISK_STATUSES,
  type RiskCategory,
  type RiskStatus,
  type RiskSummary,
  type SortDirection,
} from '../types/risk'
import { labelForEnum } from '../utils/format'

export function DashboardPage() {
  const [risks, setRisks] = useState<RiskSummary[]>([])
  const [category, setCategory] = useState<RiskCategory | ''>('')
  const [status, setStatus] = useState<RiskStatus | ''>('')
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    riskApi
      .list({
        category: category || undefined,
        status: status || undefined,
        sortDirection,
      })
      .then((data) => {
        if (active) setRisks(data)
      })
      .catch(() => {
        if (active) setError('The risk register could not be loaded. Check that the API is running and try again.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [category, status, sortDirection, reloadKey])

  const stats = useMemo(() => ({
    total: risks.length,
    open: risks.filter((risk) => risk.status !== 'CLOSED').length,
    severe: risks.filter((risk) => risk.residualSeverity === 'HIGH' || risk.residualSeverity === 'CRITICAL').length,
    unmitigated: risks.filter((risk) => risk.mitigationCount === 0).length,
  }), [risks])

  return (
    <div className="page-stack">
      <section className="hero-row">
        <div>
          <span className="eyebrow">Risk management</span>
          <h1>See what needs attention first.</h1>
          <p>Track inherent exposure, document controls, and prioritize by residual risk.</p>
        </div>
        <Link className="button button-primary" to="/risks/new">Create risk</Link>
      </section>

      <section className="stat-grid" aria-label="Current filtered risk summary">
        <article className="stat-card"><span>Visible risks</span><strong>{stats.total}</strong></article>
        <article className="stat-card"><span>Active</span><strong>{stats.open}</strong></article>
        <article className="stat-card"><span>High or critical</span><strong>{stats.severe}</strong></article>
        <article className="stat-card"><span>No mitigations</span><strong>{stats.unmitigated}</strong></article>
      </section>

      <section className="panel">
        <div className="panel-heading dashboard-heading">
          <div>
            <h2>Risk dashboard</h2>
            <p>Residual score is sorted highest first by default.</p>
          </div>
          <div className="filter-bar" aria-label="Risk filters">
            <label>
              <span>Category</span>
              <select value={category} onChange={(event) => setCategory(event.target.value as RiskCategory | '')}>
                <option value="">All categories</option>
                {RISK_CATEGORIES.map((value) => (
                  <option key={value} value={value}>{labelForEnum(value)}</option>
                ))}
              </select>
            </label>
            <label>
              <span>Status</span>
              <select value={status} onChange={(event) => setStatus(event.target.value as RiskStatus | '')}>
                <option value="">All statuses</option>
                {RISK_STATUSES.map((value) => (
                  <option key={value} value={value}>{labelForEnum(value)}</option>
                ))}
              </select>
            </label>
            <label>
              <span>Residual score</span>
              <select value={sortDirection} onChange={(event) => setSortDirection(event.target.value as SortDirection)}>
                <option value="desc">Highest first</option>
                <option value="asc">Lowest first</option>
              </select>
            </label>
          </div>
        </div>

        {loading && <div className="loading-line" role="status">Loading risks…</div>}
        {!loading && error && (
          <PageMessage
            tone="error"
            title="Unable to load risks"
            message={error}
            action={<button className="button button-secondary button-small" onClick={() => setReloadKey((value) => value + 1)}>Retry</button>}
          />
        )}
        {!loading && !error && risks.length === 0 && (
          <PageMessage
            title="No risks match these filters"
            message="Adjust the filters or create a new risk to begin the register."
          />
        )}
        {!loading && !error && risks.length > 0 && <RiskTable risks={risks} />}
      </section>
    </div>
  )
}
