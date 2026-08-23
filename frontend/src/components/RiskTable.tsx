import { Link } from 'react-router-dom'
import type { RiskSummary } from '../types/risk'
import { labelForEnum } from '../utils/format'
import { ScoreBadge } from './ScoreBadge'
import { StatusBadge } from './StatusBadge'

export function RiskTable({ risks }: { risks: RiskSummary[] }) {
  return (
    <div className="table-scroll">
      <table className="risk-table">
        <thead>
          <tr>
            <th scope="col">Risk</th>
            <th scope="col">Category</th>
            <th scope="col">Status</th>
            <th scope="col">Inherent</th>
            <th scope="col">Residual</th>
            <th scope="col">Mitigations</th>
            <th scope="col"><span className="sr-only">Actions</span></th>
          </tr>
        </thead>
        <tbody>
          {risks.map((risk) => (
            <tr key={risk.id}>
              <td>
                <Link className="risk-title-link" to={`/risks/${risk.id}`}>
                  {risk.title}
                </Link>
              </td>
              <td>{labelForEnum(risk.category)}</td>
              <td><StatusBadge status={risk.status} /></td>
              <td><ScoreBadge score={risk.inherentScore} severity={risk.inherentSeverity} compact /></td>
              <td><ScoreBadge score={risk.residualScore} severity={risk.residualSeverity} compact /></td>
              <td>
                <span className="count-pill">{risk.mitigationCount}</span>
              </td>
              <td className="table-action">
                <Link className="text-link" to={`/risks/${risk.id}`}>View</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
