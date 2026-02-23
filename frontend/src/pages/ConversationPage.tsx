import { useState, useRef, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useMessages } from '../hooks/useMessages'
import { useChat } from '../hooks/useChat'
import { useAuth } from '../contexts/AuthContext'
import type { MessageResponse } from '../api/types'

function MessageBubble({ message, isMine }: { message: MessageResponse; isMine: boolean }) {
  const time = new Date(message.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  return (
    <div className={`flex ${isMine ? 'justify-end' : 'justify-start'} mb-3`}>
      <div className={`max-w-[70%] ${isMine ? 'order-2' : ''}`}>
        {!isMine && (
          <p className="text-xs text-gray-500 mb-1">{message.senderCompanyName}</p>
        )}
        <div
          className={`px-4 py-2 rounded-2xl ${
            isMine
              ? 'bg-blue-600 text-white rounded-br-md'
              : 'bg-gray-100 text-gray-900 rounded-bl-md'
          }`}
        >
          <p className="text-sm whitespace-pre-wrap">{message.content}</p>
        </div>
        <p className={`text-xs text-gray-400 mt-1 ${isMine ? 'text-right' : ''}`}>{time}</p>
      </div>
    </div>
  )
}

export default function ConversationPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { companyId } = useAuth()
  const conversationId = id ? Number(id) : null
  const { messages, loading, hasMore, loadMore, addMessage } = useMessages(conversationId)
  const { sendMessage, sending } = useChat()
  const [input, setInput] = useState('')
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  // Auto-scroll on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages.length])

  async function handleSend(e: React.FormEvent) {
    e.preventDefault()
    if (!input.trim() || !conversationId) return

    const content = input.trim()
    setInput('')

    try {
      const msg = await sendMessage({ conversationId, content })
      addMessage(msg)
    } catch {
      setInput(content) // Restore on error
    }
  }

  function handleScroll() {
    if (!containerRef.current || !hasMore) return
    if (containerRef.current.scrollTop === 0) {
      loadMore()
    }
  }

  if (!conversationId) {
    return <div className="text-gray-400">Invalid conversation</div>
  }

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)]">
      {/* Header */}
      <div className="flex items-center gap-3 pb-4 border-b border-gray-200">
        <button
          onClick={() => navigate('/messages')}
          className="text-gray-500 hover:text-gray-700 transition-colors"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 className="text-lg font-semibold">Conversation #{conversationId}</h1>
      </div>

      {/* Messages */}
      <div
        ref={containerRef}
        onScroll={handleScroll}
        className="flex-1 overflow-y-auto py-4 px-2"
      >
        {loading && (
          <div className="text-center text-gray-400 py-4">Loading messages...</div>
        )}

        {hasMore && !loading && (
          <button
            onClick={loadMore}
            className="block mx-auto text-sm text-blue-600 hover:underline mb-4"
          >
            Load older messages
          </button>
        )}

        {messages.map((msg) => (
          <MessageBubble
            key={msg.id}
            message={msg}
            isMine={msg.senderCompanyId === companyId}
          />
        ))}

        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleSend} className="pt-4 border-t border-gray-200 flex gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type a message..."
          className="flex-1 px-4 py-2 border border-gray-300 rounded-full focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
          disabled={sending}
        />
        <button
          type="submit"
          disabled={sending || !input.trim()}
          className="bg-blue-600 text-white px-6 py-2 rounded-full text-sm hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          Send
        </button>
      </form>
    </div>
  )
}
