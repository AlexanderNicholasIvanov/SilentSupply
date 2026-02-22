import { createContext, useContext, useEffect, useRef, useState, useCallback, type ReactNode } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '../api/client'
import { useAuth } from './AuthContext'
import type { MessageResponse } from '../api/types'

type MessageHandler = (message: MessageResponse) => void

interface WebSocketContextType {
  connected: boolean
  sendMessage: (payload: { conversationId?: number; content: string }) => void
  onMessage: (handler: MessageHandler) => () => void
}

const WebSocketContext = createContext<WebSocketContextType | null>(null)

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const [connected, setConnected] = useState(false)
  const clientRef = useRef<Client | null>(null)
  const handlersRef = useRef<Set<MessageHandler>>(new Set())

  useEffect(() => {
    if (!isAuthenticated) return

    const token = getToken()
    if (!token) return

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true)
        client.subscribe('/user/queue/messages', (frame) => {
          const message: MessageResponse = JSON.parse(frame.body)
          handlersRef.current.forEach((handler) => handler(message))
        })
      },
      onDisconnect: () => {
        setConnected(false)
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'])
        setConnected(false)
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [isAuthenticated])

  const sendMessage = useCallback((payload: { conversationId?: number; content: string }) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(payload),
      })
    }
  }, [])

  const onMessage = useCallback((handler: MessageHandler) => {
    handlersRef.current.add(handler)
    return () => {
      handlersRef.current.delete(handler)
    }
  }, [])

  return (
    <WebSocketContext.Provider value={{ connected, sendMessage, onMessage }}>
      {children}
    </WebSocketContext.Provider>
  )
}

export function useWebSocketContext() {
  const context = useContext(WebSocketContext)
  if (!context) {
    throw new Error('useWebSocketContext must be used within a WebSocketProvider')
  }
  return context
}
