import { useState, useEffect, useCallback } from 'react'
import { apiClient } from '../api/client'
import type { ProductResponse, ProductSearchParams } from '../api/types'

export function useProducts(params?: ProductSearchParams) {
  const [products, setProducts] = useState<ProductResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchProducts = useCallback(async () => {
    setLoading(true)
    try {
      const query = new URLSearchParams()
      if (params?.name) query.set('name', params.name)
      if (params?.category) query.set('category', params.category)
      if (params?.minPrice != null) query.set('minPrice', String(params.minPrice))
      if (params?.maxPrice != null) query.set('maxPrice', String(params.maxPrice))
      if (params?.status) query.set('status', params.status)

      const qs = query.toString()
      const data = await apiClient<ProductResponse[]>(`/api/products${qs ? `?${qs}` : ''}`)
      setProducts(data)
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load products')
    } finally {
      setLoading(false)
    }
  }, [params?.name, params?.category, params?.minPrice, params?.maxPrice, params?.status])

  useEffect(() => {
    fetchProducts()
  }, [fetchProducts])

  return { products, loading, error, refetch: fetchProducts }
}
