import { useState, useEffect, useRef } from 'react'
import { getToken } from '../api/client'
import { useAuth } from '../contexts/AuthContext'

export function useNotifications() {
  const { isAuthenticated } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)
  const eventSourceRef = useRef<EventSource | null>(null)

  // Fetch initial unread count
  useEffect(() => {
    if (!isAuthenticated) return

    const token = getToken()
    if (!token) return

    fetch('/api/notifications/unread-count', {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => setUnreadCount(data.unreadCount))
      .catch(() => {})
  }, [isAuthenticated])

  // SSE subscription
  useEffect(() => {
    if (!isAuthenticated) return

    const token = getToken()
    if (!token) return

    const eventSource = new EventSource(`/api/notifications/stream?token=${token}`)
    eventSourceRef.current = eventSource

    eventSource.addEventListener('notification', () => {
      setUnreadCount((prev) => prev + 1)
    })

    eventSource.onerror = () => {
      eventSource.close()
    }

    return () => {
      eventSource.close()
      eventSourceRef.current = null
    }
  }, [isAuthenticated])

  return { unreadCount, setUnreadCount }
}
