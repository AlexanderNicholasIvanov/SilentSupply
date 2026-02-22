import { useCallback, useState } from 'react'
import { apiClient } from '../api/client'
import type { MessageResponse, SendMessageRequest } from '../api/types'

export function useChat() {
  const [sending, setSending] = useState(false)

  const sendMessage = useCallback(async (request: SendMessageRequest): Promise<MessageResponse> => {
    setSending(true)
    try {
      return await apiClient<MessageResponse>('/api/messages', {
        method: 'POST',
        body: JSON.stringify(request),
      })
    } finally {
      setSending(false)
    }
  }, [])

  return { sendMessage, sending }
}
