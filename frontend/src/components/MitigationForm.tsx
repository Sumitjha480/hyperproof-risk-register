import { useState } from 'react'
import { ApiClientError } from '../api/client'
import type { MitigationPayload } from '../types/risk'
import { effectivenessLabel } from '../utils/format'

interface MitigationFormProps {
  initialValues?: MitigationPayload
  submitLabel: string
  onSubmit: (payload: MitigationPayload) => Promise<void>
  onCancel?: () => void
}

export function MitigationForm({
  initialValues = { description: '', effectiveness: 3 },
  submitLabel,
  onSubmit,
  onCancel,
}: MitigationFormProps) {
  const [description, setDescription] = useState(initialValues.description)
  const [effectiveness, setEffectiveness] = useState(initialValues.effectiveness)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    if (!description.trim()) {
      setError('A mitigation description is required.')
      return
    }

    setSubmitting(true)
    try {
      await onSubmit({ description: description.trim(), effectiveness })
      if (!onCancel) {
        setDescription('')
        setEffectiveness(3)
      }
    } catch (caught) {
      setError(caught instanceof ApiClientError ? caught.message : 'The mitigation could not be saved.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="mitigation-form" onSubmit={handleSubmit}>
      {error && <div className="inline-alert" role="alert">{error}</div>}
      <label className="field">
        <span>Description</span>
        <textarea
          rows={3}
          maxLength={2000}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          placeholder="Describe the control and how it reduces this risk"
        />
      </label>
      <label className="field">
        <span>Effectiveness</span>
        <select value={effectiveness} onChange={(event) => setEffectiveness(Number(event.target.value))}>
          {[1, 2, 3, 4, 5].map((value) => (
            <option key={value} value={value}>{value} — {effectivenessLabel(value)}</option>
          ))}
        </select>
        <small>Each point reduces the remaining score by 10%; multiple controls compound.</small>
      </label>
      <div className="button-row">
        <button className="button button-primary button-small" type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : submitLabel}
        </button>
        {onCancel && (
          <button className="button button-ghost button-small" type="button" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}
