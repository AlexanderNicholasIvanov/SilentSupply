import { useState, useEffect, useCallback } from 'react'
import { apiClient } from '../api/client'
import type { OrderResponse, OrderStatus } from '../api/types'

export function useOrder(id?: number) {
  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [loading, setLoading] = useState(!!id)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    apiClient<OrderResponse>(`/api/orders/${id}`)
      .then((data) => {
        setOrder(data)
        setError(null)
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : 'Failed to load order')
      })
      .finally(() => setLoading(false))
  }, [id])

  const updateStatus = useCallback(async (orderId: number, status: OrderStatus) => {
    setSaving(true)
    try {
      const updated = await apiClient<OrderResponse>(`/api/orders/${orderId}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      })
      setOrder(updated)
      return updated
    } finally {
      setSaving(false)
    }
  }, [])

  return { order, loading, error, saving, updateStatus }
}
