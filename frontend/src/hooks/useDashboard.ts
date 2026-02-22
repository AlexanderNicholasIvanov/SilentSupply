import { useState, useEffect } from 'react'
import { apiClient } from '../api/client'
import { useAuth } from '../contexts/AuthContext'
import type { SupplierAnalytics, BuyerAnalytics } from '../api/types'

export function useDashboard() {
  const { role } = useAuth()
  const [supplierData, setSupplierData] = useState<SupplierAnalytics | null>(null)
  const [buyerData, setBuyerData] = useState<BuyerAnalytics | null>(null)
  const [unreadMessages, setUnreadMessages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function fetchData() {
      setLoading(true)
      setError(null)
      try {
        const [messagesCount] = await Promise.all([
          apiClient<{ unreadCount: number }>('/api/messages/unread-count'),
        ])
        setUnreadMessages(messagesCount.unreadCount)

        if (role === 'SUPPLIER') {
          const analytics = await apiClient<SupplierAnalytics>('/api/analytics/supplier')
          setSupplierData(analytics)
        } else if (role === 'BUYER') {
          const analytics = await apiClient<BuyerAnalytics>('/api/analytics/buyer')
          setBuyerData(analytics)
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load dashboard')
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [role])

  return { supplierData, buyerData, unreadMessages, loading, error, role }
}
