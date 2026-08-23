import type { SeverityBand } from '../types/risk'

export function inherentScore(likelihood: number, impact: number): number {
  return likelihood * impact
}

export function severityFor(score: number): SeverityBand {
  if (!Number.isInteger(score) || score < 1 || score > 25) {
    throw new Error('score must be an integer between 1 and 25')
  }
  if (score <= 5) return 'LOW'
  if (score <= 12) return 'MEDIUM'
  if (score <= 19) return 'HIGH'
  return 'CRITICAL'
}
