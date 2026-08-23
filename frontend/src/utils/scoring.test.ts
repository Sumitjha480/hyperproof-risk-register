import { describe, expect, it } from 'vitest'
import { inherentScore, severityFor } from './scoring'

describe('frontend scoring helpers', () => {
  it('computes inherent risk live from likelihood and impact', () => {
    expect(inherentScore(5, 4)).toBe(20)
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
