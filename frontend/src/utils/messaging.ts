export function getConversationUrl(_type: string, _referenceId: number | null, conversationId: number): string {
  return `/messages/${conversationId}`
}

export function formatMessageTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // Less than 1 minute
  if (diff < 60000) return 'Just now'

  // Less than 1 hour
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`

  // Same day
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  // Yesterday
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) return 'Yesterday'

  // Older
  return date.toLocaleDateString()
}
