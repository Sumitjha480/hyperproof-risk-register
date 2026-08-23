import { describe, expect, it } from 'vitest'
import { inherentScore, residualScore, severityFor } from './scoring'

describe('frontend scoring helpers', () => {
  it('computes inherent risk live from likelihood and impact', () => {
    expect(inherentScore(5, 4)).toBe(20)
  })

 it('calculates residual score using compound mitigation reduction', () => {
   expect(residualScore(20, [5])).toBe(10)
   expect(residualScore(20, [3, 4])).toBe(9)
   expect(residualScore(1, [5, 5, 5])).toBe(1)
 })

  it.each([
    [1, 'LOW'],
    [5, 'LOW'],
    [6, 'MEDIUM'],
    [12, 'MEDIUM'],
    [13, 'HIGH'],
    [19, 'HIGH'],
    [20, 'CRITICAL'],
    [25, 'CRITICAL'],
  ] as const)('maps score %s to %s', (score, expected) => {
    expect(severityFor(score)).toBe(expected)
  })
})
