interface PageMessageProps {
  title: string
  message: string
  tone?: 'neutral' | 'error'
  action?: React.ReactNode
}

export function PageMessage({ title, message, tone = 'neutral', action }: PageMessageProps) {
  return (
    <div className={`page-message page-message-${tone}`} role={tone === 'error' ? 'alert' : undefined}>
      <h2>{title}</h2>
      <p>{message}</p>
      {action}
    </div>
  )
}
