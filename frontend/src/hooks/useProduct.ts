import { useState, useEffect, useCallback } from 'react'
import { apiClient } from '../api/client'
import type { ProductResponse, ProductRequest } from '../api/types'

export function useProduct(id?: number) {
  const [product, setProduct] = useState<ProductResponse | null>(null)
  const [loading, setLoading] = useState(!!id)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    apiClient<ProductResponse>(`/api/products/${id}`)
      .then((data) => {
        setProduct(data)
        setError(null)
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : 'Failed to load product')
      })
      .finally(() => setLoading(false))
  }, [id])

  const createProduct = useCallback(async (request: ProductRequest): Promise<ProductResponse> => {
    setSaving(true)
    try {
      return await apiClient<ProductResponse>('/api/products', {
        method: 'POST',
        body: JSON.stringify(request),
      })
    } finally {
      setSaving(false)
    }
  }, [])

  const updateProduct = useCallback(async (productId: number, request: ProductRequest): Promise<ProductResponse> => {
    setSaving(true)
    try {
      return await apiClient<ProductResponse>(`/api/products/${productId}`, {
        method: 'PUT',
        body: JSON.stringify(request),
      })
    } finally {
      setSaving(false)
    }
  }, [])

  const deleteProduct = useCallback(async (productId: number): Promise<void> => {
    await apiClient<void>(`/api/products/${productId}`, { method: 'DELETE' })
  }, [])

  return { product, loading, error, saving, createProduct, updateProduct, deleteProduct }
}
