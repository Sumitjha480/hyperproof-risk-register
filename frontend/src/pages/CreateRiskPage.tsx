import { useNavigate } from 'react-router-dom'
import { riskApi } from '../api/client'
import { RiskForm } from '../components/RiskForm'
import type { RiskPayload } from '../types/risk'

export function CreateRiskPage() {
  const navigate = useNavigate()

  async function create(values: RiskPayload) {
    const risk = await riskApi.create(values)
    navigate(`/risks/${risk.id}`)
  }

  return (
    <div className="narrow-page page-stack">
      <section className="page-heading">
        <span className="eyebrow">New record</span>
        <h1>Create a risk</h1>
        <p>Start with the pre-control assessment, schedule its next review, and optionally map it to NIST CSF functions.</p>
      </section>
      <RiskForm submitLabel="Create risk" onSubmit={create} />
    </div>
  )
}
