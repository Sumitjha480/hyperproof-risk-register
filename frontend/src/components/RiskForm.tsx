import { useMemo, useState } from 'react'
import { ApiClientError } from '../api/client'
import {
  RISK_CATEGORIES,
  RISK_STATUSES,
  type RiskPayload,
  type RiskStatus,
} from '../types/risk'
import { labelForEnum } from '../utils/format'
import { inherentScore, severityFor } from '../utils/scoring'
import { ScoreBadge } from './ScoreBadge'

interface RiskFormProps {
  initialValues?: RiskPayload
  mitigationCount?: number
  submitLabel: string
  onSubmit: (values: RiskPayload) => Promise<void>
}

const defaultValues: RiskPayload = {
  title: '',
  description: '',
  category: 'OPERATIONAL',
  owner: '',
  likelihood: 3,
  impact: 3,
  status: 'OPEN',
}

export function RiskForm({
  initialValues = defaultValues,
  mitigationCount = 0,
  submitLabel,
  onSubmit,
}: RiskFormProps) {
  const [values, setValues] = useState<RiskPayload>(initialValues)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  const liveScore = useMemo(
    () => inherentScore(values.likelihood, values.impact),
    [values.likelihood, values.impact],
  )

  function update<K extends keyof RiskPayload>(field: K, value: RiskPayload[K]) {
    setValues((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => {
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError('')
    const localErrors: Record<string, string> = {}
    if (!values.title.trim()) localErrors.title = 'Title is required'
    if (!values.owner.trim()) localErrors.owner = 'Owner is required'
    if (Object.keys(localErrors).length > 0) {
      setFieldErrors(localErrors)
      return
    }

    setSubmitting(true)
    try {
      await onSubmit({
        ...values,
        title: values.title.trim(),
        owner: values.owner.trim(),
        description: values.description.trim(),
      })
    } catch (error) {
      if (error instanceof ApiClientError) {
        setFieldErrors(error.fieldErrors)
        setFormError(error.message)
      } else {
        setFormError('The risk could not be saved. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const closedDisabled = mitigationCount === 0

  return (
    <form className="form-card" onSubmit={handleSubmit} noValidate>
      {formError && <div className="inline-alert" role="alert">{formError}</div>}

      <div className="form-section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Identity</span>
            <h2>Risk details</h2>
          </div>
          <p>Describe the event clearly enough that its owner and an auditor can understand it.</p>
        </div>

        <div className="form-grid">
          <label className="field field-span-2">
            <span>Title</span>
            <input
              autoFocus
              maxLength={200}
              value={values.title}
              onChange={(event) => update('title', event.target.value)}
              aria-invalid={Boolean(fieldErrors.title)}
            />
            {fieldErrors.title && <small className="field-error">{fieldErrors.title}</small>}
          </label>

          <label className="field field-span-2">
            <span>Description</span>
            <textarea
              rows={5}
              maxLength={5000}
              value={values.description}
              onChange={(event) => update('description', event.target.value)}
              aria-invalid={Boolean(fieldErrors.description)}
            />
            {fieldErrors.description && <small className="field-error">{fieldErrors.description}</small>}
          </label>

          <label className="field">
            <span>Category</span>
            <select
              value={values.category}
              onChange={(event) => update('category', event.target.value as RiskPayload['category'])}
            >
              {RISK_CATEGORIES.map((category) => (
                <option key={category} value={category}>{labelForEnum(category)}</option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Owner</span>
            <input
              maxLength={200}
              value={values.owner}
              onChange={(event) => update('owner', event.target.value)}
              aria-invalid={Boolean(fieldErrors.owner)}
            />
            {fieldErrors.owner && <small className="field-error">{fieldErrors.owner}</small>}
          </label>
        </div>
      </div>

      <div className="form-section">
        <div className="section-heading score-heading">
          <div>
            <span className="eyebrow">Assessment</span>
            <h2>Inherent risk</h2>
          </div>
          <ScoreBadge score={liveScore} severity={severityFor(liveScore)} />
        </div>

        <div className="form-grid">
          <label className="field">
            <span>Likelihood</span>
            <select
              value={values.likelihood}
              onChange={(event) => update('likelihood', Number(event.target.value))}
            >
              {[1, 2, 3, 4, 5].map((value) => (
                <option key={value} value={value}>{value}</option>
              ))}
            </select>
            <small>1 = rare, 5 = almost certain</small>
          </label>

          <label className="field">
            <span>Impact</span>
            <select
              value={values.impact}
              onChange={(event) => update('impact', Number(event.target.value))}
            >
              {[1, 2, 3, 4, 5].map((value) => (
                <option key={value} value={value}>{value}</option>
              ))}
            </select>
            <small>1 = minimal, 5 = severe</small>
          </label>

          <label className="field field-span-2">
            <span>Status</span>
            <select
              value={values.status}
              onChange={(event) => update('status', event.target.value as RiskStatus)}
            >
              {RISK_STATUSES.map((status) => (
                <option key={status} value={status} disabled={status === 'CLOSED' && closedDisabled}>
                  {labelForEnum(status)}
                </option>
              ))}
            </select>
            {closedDisabled && (
              <small>A risk can be closed only after at least one mitigation is recorded.</small>
            )}
          </label>
        </div>
      </div>

      <div className="form-actions">
        <button className="button button-primary" type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  )
}
