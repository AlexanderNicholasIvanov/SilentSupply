import { useState, useEffect, useCallback } from 'react'
import { apiClient } from '../api/client'
import { useWebSocketContext } from '../contexts/WebSocketContext'
import type { MessageResponse, PageResponse } from '../api/types'

export function useMessages(conversationId: number | null) {
  const [messages, setMessages] = useState<MessageResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [hasMore, setHasMore] = useState(false)
  const [page, setPage] = useState(0)
  const { onMessage } = useWebSocketContext()

  const fetchMessages = useCallback(async (pageNum: number) => {
    if (!conversationId) return
    try {
      const data = await apiClient<PageResponse<MessageResponse>>(
        `/api/messages/conversations/${conversationId}?page=${pageNum}&size=50`
      )
      if (pageNum === 0) {
        setMessages(data.content.reverse())
      } else {
        setMessages((prev) => [...data.content.reverse(), ...prev])
      }
      setHasMore(!data.last)
      setPage(pageNum)
    } catch {
      // Silently handle errors on pagination
    } finally {
      setLoading(false)
    }
  }, [conversationId])

  useEffect(() => {
    setMessages([])
    setPage(0)
    setLoading(true)
    fetchMessages(0)
  }, [fetchMessages])

  // Append real-time messages
  useEffect(() => {
    return onMessage((message: MessageResponse) => {
      if (message.conversationId === conversationId) {
        setMessages((prev) => {
          // Deduplicate by ID
          if (prev.some((m) => m.id === message.id)) return prev
          return [...prev, message]
        })
      }
    })
  }, [onMessage, conversationId])

  const loadMore = useCallback(() => {
    if (hasMore) {
      fetchMessages(page + 1)
    }
  }, [fetchMessages, hasMore, page])

  // Mark as read
  useEffect(() => {
    if (!conversationId) return
    apiClient<void>(`/api/messages/conversations/${conversationId}/read`, { method: 'PATCH' }).catch(() => {})
  }, [conversationId, messages.length])

  return { messages, loading, hasMore, loadMore }
}
