import type { RiskStatus } from '../types/risk'
import { labelForEnum } from '../utils/format'

export function StatusBadge({ status }: { status: RiskStatus }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{labelForEnum(status)}</span>
}
