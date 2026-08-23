import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { riskApi } from '../api/client'
import { PageMessage } from '../components/PageMessage'
import { RiskForm } from '../components/RiskForm'
import type { RiskDetail, RiskPayload } from '../types/risk'

export function EditRiskPage() {
  const { riskId } = useParams<{ riskId: string }>()
  const navigate = useNavigate()
  const [risk, setRisk] = useState<RiskDetail | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!riskId) return
    riskApi.get(riskId).then(setRisk).catch(() => setError('The requested risk could not be loaded.'))
  }, [riskId])

  if (!riskId) return null
  if (error) return <PageMessage tone="error" title="Unable to edit risk" message={error} />
  if (!risk) return <div className="loading-line" role="status">Loading risk…</div>

  const initialValues: RiskPayload = {
    title: risk.title,
    description: risk.description ?? '',
    category: risk.category,
    owner: risk.owner,
    likelihood: risk.likelihood,
    impact: risk.impact,
    status: risk.status,
  }

  async function save(values: RiskPayload) {
    const updated = await riskApi.update(riskId!, values)
    navigate(`/risks/${updated.id}`)
  }

  return (
    <div className="narrow-page page-stack">
      <section className="page-heading">
        <span className="eyebrow">Edit record</span>
        <h1>Edit risk</h1>
        <p>Changes to likelihood or impact immediately recalculate both scores after save.</p>
      </section>
      <RiskForm
        initialValues={initialValues}
        mitigationCount={risk.mitigationCount}
        submitLabel="Save changes"
        onSubmit={save}
      />
    </div>
  )
}
