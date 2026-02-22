import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useConversations } from '../hooks/useConversations'
import { useChat } from '../hooks/useChat'
import type { ConversationResponse } from '../api/types'

function ConversationItem({ conversation, onClick }: { conversation: ConversationResponse; onClick: () => void }) {
  const otherParticipants = conversation.participants
    .map((p) => p.companyName)
    .join(', ')

  const title = conversation.subject || otherParticipants
  const time = conversation.lastMessageAt
    ? new Date(conversation.lastMessageAt).toLocaleString()
    : new Date(conversation.createdAt).toLocaleString()

  return (
    <button
      onClick={onClick}
      className="w-full text-left px-4 py-3 hover:bg-gray-50 border-b border-gray-100 flex items-start gap-3 transition-colors"
    >
      <div className="w-10 h-10 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center flex-shrink-0 text-sm font-medium">
        {conversation.type === 'DIRECT' ? 'DM' : conversation.type}
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex justify-between items-center">
          <p className="font-medium text-sm truncate">{title}</p>
          <span className="text-xs text-gray-400 flex-shrink-0 ml-2">{time}</span>
        </div>
        {conversation.lastMessagePreview && (
          <p className="text-sm text-gray-500 truncate mt-0.5">
            {conversation.lastMessageSenderName}: {conversation.lastMessagePreview}
          </p>
        )}
      </div>

      {conversation.unreadCount > 0 && (
        <span className="bg-blue-600 text-white text-xs rounded-full px-2 py-0.5 flex-shrink-0">
          {conversation.unreadCount}
        </span>
      )}
    </button>
  )
}

export default function MessagesPage() {
  const { conversations, loading, error } = useConversations()
  const { sendMessage } = useChat()
  const navigate = useNavigate()
  const [showNew, setShowNew] = useState(false)
  const [recipientId, setRecipientId] = useState('')
  const [newMessage, setNewMessage] = useState('')
  const [filter, setFilter] = useState<string>('ALL')

  const filtered = filter === 'ALL'
    ? conversations
    : conversations.filter((c) => c.type === filter)

  async function handleNewConversation(e: React.FormEvent) {
    e.preventDefault()
    if (!recipientId || !newMessage.trim()) return
    try {
      const response = await sendMessage({
        recipientCompanyId: Number(recipientId),
        content: newMessage.trim(),
      })
      setShowNew(false)
      setRecipientId('')
      setNewMessage('')
      navigate(`/messages/${response.conversationId}`)
    } catch {
      // Error handled by useChat
    }
  }

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-gray-400">Loading conversations...</div>
  }

  if (error) {
    return <div className="bg-red-50 text-red-600 p-4 rounded-lg">{error}</div>
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Messages</h1>
        <button
          onClick={() => setShowNew(!showNew)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700 transition-colors"
        >
          New Message
        </button>
      </div>

      {showNew && (
        <form onSubmit={handleNewConversation} className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6 space-y-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Recipient Company ID</label>
            <input
              type="number"
              value={recipientId}
              onChange={(e) => setRecipientId(e.target.value)}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
              placeholder="Enter company ID"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Message</label>
            <textarea
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              required
              rows={2}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none"
              placeholder="Type your message..."
            />
          </div>
          <div className="flex gap-2">
            <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700">
              Send
            </button>
            <button type="button" onClick={() => setShowNew(false)} className="text-gray-500 px-4 py-2 text-sm">
              Cancel
            </button>
          </div>
        </form>
      )}

      <div className="flex gap-2 mb-4">
        {['ALL', 'DIRECT', 'RFQ', 'ORDER'].map((type) => (
          <button
            key={type}
            onClick={() => setFilter(type)}
            className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
              filter === type
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {type === 'ALL' ? 'All' : type}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {filtered.length === 0 ? (
          <div className="p-8 text-center text-gray-400">No conversations yet</div>
        ) : (
          filtered.map((conv) => (
            <ConversationItem
              key={conv.id}
              conversation={conv}
              onClick={() => navigate(`/messages/${conv.id}`)}
            />
          ))
        )}
      </div>
    </div>
  )
}
