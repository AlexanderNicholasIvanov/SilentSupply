import { useState, useEffect, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useConversations } from '../hooks/useConversations'
import { useChat } from '../hooks/useChat'
import { useAuth } from '../contexts/AuthContext'
import { apiClient } from '../api/client'
import type { CompanyResponse, ConversationResponse } from '../api/types'

function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/[\s]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
    .slice(0, 60)
}

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
  const { role } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [showNew, setShowNew] = useState(false)
  const [recipientId, setRecipientId] = useState('')
  const [subject, setSubject] = useState('')
  const [newMessage, setNewMessage] = useState('')
  const [filter, setFilter] = useState<string>('ALL')
  const [companies, setCompanies] = useState<CompanyResponse[]>([])
  const [companySearch, setCompanySearch] = useState('')
  const [showDropdown, setShowDropdown] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (showNew && role) {
      const oppositeRole = role === 'BUYER' ? 'SUPPLIER' : 'BUYER'
      apiClient<CompanyResponse[]>(`/api/companies?role=${oppositeRole}`)
        .then(setCompanies)
        .catch(() => setCompanies([]))
    }
  }, [showNew, role])

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowDropdown(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  // Pre-fill from "Contact Supplier" navigation
  useEffect(() => {
    const state = location.state as { recipientId?: number; subject?: string } | null
    if (state?.recipientId) {
      setRecipientId(String(state.recipientId))
      if (state.subject) setSubject(state.subject)
      setShowNew(true)
      // Clear location state so it doesn't re-trigger
      navigate(location.pathname, { replace: true, state: null })
    }
  }, [location.state, navigate, location.pathname])

  const filteredCompanies = companies.filter((c) =>
    c.name.toLowerCase().includes(companySearch.toLowerCase())
  )

  const selectedCompany = companies.find((c) => c.id === Number(recipientId))

  const filtered = filter === 'ALL'
    ? conversations
    : conversations.filter((c) => c.type === filter)

  async function handleNewConversation(e: React.FormEvent) {
    e.preventDefault()
    if (!recipientId || !newMessage.trim()) return
    try {
      const trimmedSubject = subject.trim() || undefined
      const response = await sendMessage({
        recipientCompanyId: Number(recipientId),
        subject: trimmedSubject,
        content: newMessage.trim(),
      })
      setShowNew(false)
      setRecipientId('')
      setSubject('')
      setNewMessage('')
      setCompanySearch('')
      const slug = trimmedSubject ? '/' + slugify(trimmedSubject) : ''
      navigate(`/messages/${response.conversationId}${slug}`)
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
          <div ref={dropdownRef} className="relative">
            <label className="block text-sm font-medium text-gray-700 mb-1">Recipient</label>
            <input
              type="text"
              value={selectedCompany ? selectedCompany.name : companySearch}
              onChange={(e) => {
                setCompanySearch(e.target.value)
                setRecipientId('')
                setShowDropdown(true)
              }}
              onFocus={() => setShowDropdown(true)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
              placeholder="Search for a company..."
            />
            <input type="hidden" name="recipientId" value={recipientId} required />
            {showDropdown && (
              <div className="absolute z-10 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg max-h-48 overflow-y-auto">
                {filteredCompanies.length === 0 ? (
                  <div className="px-3 py-2 text-sm text-gray-400">No companies found</div>
                ) : (
                  filteredCompanies.map((c) => (
                    <button
                      key={c.id}
                      type="button"
                      onClick={() => {
                        setRecipientId(String(c.id))
                        setCompanySearch('')
                        setShowDropdown(false)
                      }}
                      className="w-full text-left px-3 py-2 text-sm hover:bg-blue-50 flex justify-between items-center"
                    >
                      <span className="font-medium">{c.name}</span>
                      <span className="text-xs text-gray-400">{c.role}</span>
                    </button>
                  ))
                )}
              </div>
            )}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Subject (optional)</label>
            <input
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              maxLength={255}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
              placeholder="e.g. Bulk order inquiry"
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
              onClick={() => {
                const slug = conv.subject ? '/' + slugify(conv.subject) : ''
                navigate(`/messages/${conv.id}${slug}`)
              }}
            />
          ))
        )}
      </div>
    </div>
  )
}
