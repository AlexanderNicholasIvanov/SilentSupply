import { useState, useEffect, useCallback } from 'react'
import { apiClient } from '../api/client'
import { useWebSocketContext } from '../contexts/WebSocketContext'
import type { ConversationResponse, MessageResponse } from '../api/types'

export function useConversations() {
  const [conversations, setConversations] = useState<ConversationResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { onMessage } = useWebSocketContext()

  const fetchConversations = useCallback(async () => {
    try {
      const data = await apiClient<ConversationResponse[]>('/api/messages/conversations')
      setConversations(data)
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load conversations')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchConversations()
  }, [fetchConversations])

  // Real-time updates: bump conversation to top on new message
  useEffect(() => {
    return onMessage((message: MessageResponse) => {
      setConversations((prev) => {
        const updated = prev.map((conv) => {
          if (conv.id === message.conversationId) {
            return {
              ...conv,
              lastMessagePreview: message.content.substring(0, 100),
              lastMessageSenderName: message.senderCompanyName,
              lastMessageAt: message.createdAt,
              unreadCount: conv.unreadCount + 1,
            }
          }
          return conv
        })

        // Sort by last message time (most recent first)
        return updated.sort((a, b) => {
          const timeA = a.lastMessageAt || a.createdAt
          const timeB = b.lastMessageAt || b.createdAt
          return timeB.localeCompare(timeA)
        })
      })
    })
  }, [onMessage])

  return { conversations, loading, error, refetch: fetchConversations }
}
