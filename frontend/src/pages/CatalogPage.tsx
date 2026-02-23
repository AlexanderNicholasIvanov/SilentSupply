import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useProducts } from '../hooks/useProducts'
import type { ProductSearchParams, ProductStatus } from '../api/types'

const STATUS_COLORS: Record<ProductStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-800',
  OUT_OF_STOCK: 'bg-yellow-100 text-yellow-800',
  DISCONTINUED: 'bg-red-100 text-red-800',
}

export default function CatalogPage() {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [category, setCategory] = useState('')
  const [status, setStatus] = useState<ProductStatus | ''>('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [appliedParams, setAppliedParams] = useState<ProductSearchParams>({})

  const { products, loading, error } = useProducts(appliedParams)

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    const params: ProductSearchParams = {}
    if (name.trim()) params.name = name.trim()
    if (category.trim()) params.category = category.trim()
    if (status) params.status = status
    if (minPrice) params.minPrice = Number(minPrice)
    if (maxPrice) params.maxPrice = Number(maxPrice)
    setAppliedParams(params)
  }

  const statusFilters: (ProductStatus | '')[] = ['', 'ACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED']
  const statusLabels: Record<string, string> = {
    '': 'All',
    ACTIVE: 'Active',
    OUT_OF_STOCK: 'Out of Stock',
    DISCONTINUED: 'Discontinued',
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Product Catalog</h1>

      <form onSubmit={handleSearch} className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3 mb-3">
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Search by name..."
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
          />
          <input
            type="text"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            placeholder="Category"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
          />
          <input
            type="number"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            placeholder="Min price"
            min="0"
            step="0.01"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
          />
          <input
            type="number"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            placeholder="Max price"
            min="0"
            step="0.01"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
          />
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <div className="flex gap-1">
            {statusFilters.map((s) => (
              <button
                key={s}
                type="button"
                onClick={() => setStatus(s)}
                className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
                  status === s
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {statusLabels[s]}
              </button>
            ))}
          </div>
          <button
            type="submit"
            className="ml-auto bg-blue-600 text-white px-4 py-1.5 rounded-lg text-sm hover:bg-blue-700 transition-colors"
          >
            Search
          </button>
        </div>
      </form>

      {loading && (
        <div className="flex items-center justify-center h-64 text-gray-400">Loading products...</div>
      )}

      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-lg">{error}</div>
      )}

      {!loading && !error && products.length === 0 && (
        <div className="text-center text-gray-400 py-16">No products found</div>
      )}

      {!loading && !error && products.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {products.map((product) => (
            <button
              key={product.id}
              onClick={() => navigate(`/catalog/${product.id}`)}
              className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 text-left hover:shadow-md transition-shadow"
            >
              <div className="flex justify-between items-start mb-2">
                <h3 className="font-semibold text-sm truncate flex-1">{product.name}</h3>
                <span className={`ml-2 px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[product.status]}`}>
                  {product.status.replace('_', ' ')}
                </span>
              </div>
              <p className="text-xs text-gray-500 mb-2">{product.supplierName}</p>
              {product.category && (
                <span className="inline-block bg-gray-100 text-gray-600 text-xs px-2 py-0.5 rounded mb-2">
                  {product.category}
                </span>
              )}
              <p className="text-lg font-bold text-blue-600">
                {product.currency} {Number(product.basePrice).toFixed(2)}
              </p>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
