export const RISK_CATEGORIES = [
  'OPERATIONAL',
  'FINANCIAL',
  'COMPLIANCE',
  'SECURITY',
  'STRATEGIC',
] as const

export const RISK_STATUSES = ['OPEN', 'MITIGATING', 'CLOSED'] as const
export const SEVERITY_BANDS = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const

export type RiskCategory = (typeof RISK_CATEGORIES)[number]
export type RiskStatus = (typeof RISK_STATUSES)[number]
export type SeverityBand = (typeof SEVERITY_BANDS)[number]
export type SortDirection = 'asc' | 'desc'

export interface RiskSummary {
  id: string
  title: string
  category: RiskCategory
  status: RiskStatus
  inherentScore: number
  inherentSeverity: SeverityBand
  residualScore: number
  residualSeverity: SeverityBand
  mitigationCount: number
  createdAt: string
  updatedAt: string
}

export interface Mitigation {
  id: string
  riskId: string
  description: string
  effectiveness: number
  createdAt: string
}

export interface RiskDetail extends RiskSummary {
  description: string | null
  owner: string
  likelihood: number
  impact: number
  mitigations: Mitigation[]
}

export interface RiskPayload {
  title: string
  description: string
  category: RiskCategory
  owner: string
  likelihood: number
  impact: number
  status: RiskStatus
}

export interface MitigationPayload {
  description: string
  effectiveness: number
}

export interface ApiErrorBody {
  timestamp?: string
  status?: number
  error?: string
  code?: string
  message?: string
  path?: string
  fieldErrors?: Record<string, string>
}
