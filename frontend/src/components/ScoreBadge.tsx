import type { SeverityBand } from '../types/risk'
import { labelForEnum } from '../utils/format'

interface ScoreBadgeProps {
  score: number
  severity: SeverityBand
  compact?: boolean
}

export function ScoreBadge({ score, severity, compact = false }: ScoreBadgeProps) {
  return (
    <span className={`score-badge severity-${severity.toLowerCase()} ${compact ? 'score-badge-compact' : ''}`}>
      <strong>{score}</strong>
      <span>{labelForEnum(severity)}</span>
    </span>
  )
}
