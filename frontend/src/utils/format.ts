export function labelForEnum(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

export function formatDate(value: string): string {
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(value))
}

export function effectivenessLabel(effectiveness: number): string {
  const labels: Record<number, string> = {
    1: 'Minimal',
    2: 'Limited',
    3: 'Moderate',
    4: 'Strong',
    5: 'Very strong',
  }
  return labels[effectiveness] ?? String(effectiveness)
}
