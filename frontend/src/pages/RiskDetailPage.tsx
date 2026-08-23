import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiClientError, riskApi } from '../api/client'
import { MitigationForm } from '../components/MitigationForm'
import { PageMessage } from '../components/PageMessage'
import { ScoreBadge } from '../components/ScoreBadge'
import { StatusBadge } from '../components/StatusBadge'
import type { Mitigation, MitigationPayload, RiskDetail } from '../types/risk'
import { effectivenessLabel, formatDate, labelForEnum } from '../utils/format'
import { residualScore, severityFor } from '../utils/scoring'

function applyOptimisticMitigations(risk: RiskDetail, mitigations: Mitigation[]): RiskDetail {
 const residual = residualScore(risk.inherentScore, mitigations.map((item) => item.effectiveness))
 return {
   ...risk,
   mitigations,
   mitigationCount: mitigations.length,
   residualScore: residual,
   residualSeverity: severityFor(residual),
 }
}

export function RiskDetailPage() {
  const { riskId } = useParams<{ riskId: string }>()
  const navigate = useNavigate()
  const [risk, setRisk] = useState<RiskDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [editingMitigationId, setEditingMitigationId] = useState<string | null>(null)
  const [optimisticAction, setOptimisticAction] = useState('')

  const loadRisk = useCallback(async () => {
    if (!riskId) return
    setLoading(true)
    setError('')
    try {
      setRisk(await riskApi.get(riskId))
    } catch {
      setError('The requested risk could not be loaded.')
    } finally {
      setLoading(false)
    }
  }, [riskId])

  useEffect(() => {
    void loadRisk()
  }, [loadRisk])

  if (!riskId) return null
  if (loading) return <div className="loading-line" role="status">Loading risk…</div>
  if (error || !risk) return <PageMessage tone="error" title="Risk not available" message={error || 'Risk not found.'} />

  const resolvedRiskId = riskId
  const currentRisk = risk

  async function addMitigation(payload: MitigationPayload) {
   setActionError('')
   setOptimisticAction('Saving mitigation…')
   const temporaryId = `optimistic-${crypto.randomUUID()}`
   const optimisticMitigation: Mitigation = {
     id: temporaryId,
     riskId: resolvedRiskId,
     description: payload.description,
     effectiveness: payload.effectiveness,
     createdAt: new Date().toISOString(),
   }
   const previous = currentRisk
   setRisk(applyOptimisticMitigations(previous, [...previous.mitigations, optimisticMitigation]))
   try {
     await riskApi.addMitigation(resolvedRiskId, payload)
     await loadRisk()
   } catch (caught) {
     setRisk(previous)
     throw caught
   } finally {
     setOptimisticAction('')
   }
 }

 async function updateMitigation(mitigationId: string, payload: MitigationPayload) {
   setActionError('')
   setOptimisticAction('Saving mitigation…')
   const previous = currentRisk
   const updated = currentRisk.mitigations.map((mitigation) =>
     mitigation.id === mitigationId ? { ...mitigation, ...payload } : mitigation,
   )
   setRisk(applyOptimisticMitigations(currentRisk, updated))
   try {
     await riskApi.updateMitigation(resolvedRiskId, mitigationId, payload)
     setEditingMitigationId(null)
     await loadRisk()
   } catch (caught) {
     setRisk(previous)
     throw caught
   } finally {
     setOptimisticAction('')
   }
 }

  async function deleteMitigation(mitigation: Mitigation) {
    if (!window.confirm('Delete this mitigation? The residual score will be recalculated.')) return
    setActionError('')
    setOptimisticAction('Deleting mitigation…')
    const previous = currentRisk
    setRisk(applyOptimisticMitigations(currentRisk, currentRisk.mitigations.filter((item) => item.id !== mitigation.id)))
    try {
      await riskApi.deleteMitigation(resolvedRiskId, mitigation.id)
      await loadRisk()
    } catch (caught) {
      setRisk(previous)
      setActionError(caught instanceof ApiClientError ? caught.message : 'The mitigation could not be deleted.')
    } finally {
      setOptimisticAction('')
    }
  }

  async function deleteRisk() {
    if (!window.confirm(`Delete “${currentRisk.title}” and all of its mitigations?`)) return
    setActionError('')
    try {
      await riskApi.delete(resolvedRiskId)
      navigate('/')
    } catch (caught) {
      setActionError(caught instanceof ApiClientError ? caught.message : 'The risk could not be deleted.')
    }
  }

  const scoreReduction = risk.inherentScore - risk.residualScore

  return (
    <div className="page-stack">
      <section className="detail-header">
        <div>
          <div className="detail-kicker">
            <span>{labelForEnum(risk.category)}</span>
            <StatusBadge status={risk.status} />
           {risk.reviewOverdue && <span className="overdue-badge">Review overdue</span>}
          </div>
          <h1>{risk.title}</h1>
          <p>{risk.description || 'No description provided.'}</p>
        </div>
        <div className="button-row">
          <Link className="button button-secondary" to={`/risks/${risk.id}/edit`}>Edit risk</Link>
          <button className="button button-danger" type="button" onClick={deleteRisk}>Delete</button>
        </div>
      </section>

      {actionError && <div className="inline-alert" role="alert">{actionError}</div>}
      {optimisticAction && <div className="optimistic-note" role="status">{optimisticAction} The screen is showing the expected result while the server confirms it.</div>}

      <section className="score-grid">
        <article className="score-card">
          <span className="eyebrow">Before controls</span>
          <div className="score-card-main">
            <div>
              <h2>Inherent risk</h2>
              <p>{risk.likelihood} likelihood × {risk.impact} impact</p>
            </div>
            <ScoreBadge score={risk.inherentScore} severity={risk.inherentSeverity} />
          </div>
        </article>
        <article className="score-card score-card-emphasis">
          <span className="eyebrow">After controls</span>
          <div className="score-card-main">
            <div>
              <h2>Residual risk</h2>
              <p>{risk.mitigationCount} recorded mitigation{risk.mitigationCount === 1 ? '' : 's'} · {scoreReduction} point reduction</p>
            </div>
            <ScoreBadge score={risk.residualScore} severity={risk.residualSeverity} />
          </div>
        </article>
      </section>

      <section className="detail-grid">
        <article className="panel detail-metadata">
          <div className="panel-heading"><h2>Ownership and lifecycle</h2></div>
          <dl>
            <div><dt>Owner</dt><dd>{risk.owner}</dd></div>
            <div><dt>Status</dt><dd><StatusBadge status={risk.status} /></dd></div>
            <div><dt>Created</dt><dd>{formatDate(risk.createdAt)}</dd></div>
            <div><dt>Last updated</dt><dd>{formatDate(risk.updatedAt)}</dd></div>
            <div><dt>Next review</dt><dd>{risk.nextReviewDate ? formatDate(risk.nextReviewDate) : 'Not scheduled'}</dd></div>
          </dl>
          {risk.reviewOverdue && (
            <div className="rule-note rule-note-danger">
              <strong>Review overdue</strong>
              <span>The next review date has passed. Update the review date or complete the review.</span>
            </div>
          )}
          {risk.frameworkFunctions.length > 0 && (
            <div className="framework-summary">
              <span className="eyebrow">NIST CSF mapping</span>
              <div className="mapping-chips">
                {risk.frameworkFunctions.map((value) => <span className="mapping-chip" key={value}>{labelForEnum(value)}</span>)}
              </div>
            </div>
          )}
          {risk.mitigationCount === 0 && (
            <div className="rule-note">
              <strong>Closure blocked</strong>
              <span>Record at least one mitigation before marking this risk Closed.</span>
            </div>
          )}
        </article>

        <article className="panel mitigation-panel">
          <div className="panel-heading">
            <div>
              <h2>Mitigations</h2>
              <p>Controls reduce the remaining score independently and with diminishing returns.</p>
            </div>
            <span className="count-pill">{risk.mitigationCount}</span>
          </div>

          {risk.mitigations.length === 0 ? (
            <div className="empty-inline">No mitigations have been recorded.</div>
          ) : (
            <ul className="mitigation-list">
              {risk.mitigations.map((mitigation) => (
                <li key={mitigation.id}>
                  {editingMitigationId === mitigation.id ? (
                    <MitigationForm
                      initialValues={{ description: mitigation.description, effectiveness: mitigation.effectiveness }}
                      submitLabel="Save mitigation"
                      onSubmit={(payload) => updateMitigation(mitigation.id, payload)}
                      onCancel={() => setEditingMitigationId(null)}
                    />
                  ) : (
                    <>
                      <div className="mitigation-content">
                        <p>{mitigation.description}</p>
                        <span>
                          Effectiveness {mitigation.effectiveness}/5 · {effectivenessLabel(mitigation.effectiveness)} · Added {formatDate(mitigation.createdAt)}
                        </span>
                      </div>
                      <div className="mitigation-actions">
                        <button className="text-button" type="button" onClick={() => setEditingMitigationId(mitigation.id)}>Edit</button>
                        <button className="text-button text-button-danger" type="button" onClick={() => deleteMitigation(mitigation)}>Delete</button>
                      </div>
                    </>
                  )}
                </li>
              ))}
            </ul>
          )}

          <div className="add-mitigation">
            <h3>Add a mitigation</h3>
            <MitigationForm submitLabel="Add mitigation" onSubmit={addMitigation} />
          </div>
        </article>
      </section>
    </div>
  )
}
