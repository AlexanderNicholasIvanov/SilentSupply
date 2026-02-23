import { useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useProducts } from '../hooks/useProducts'
import { useProduct } from '../hooks/useProduct'
import { useAuth } from '../contexts/AuthContext'
import type { ProductStatus } from '../api/types'

const STATUS_COLORS: Record<ProductStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-800',
  OUT_OF_STOCK: 'bg-yellow-100 text-yellow-800',
  DISCONTINUED: 'bg-red-100 text-red-800',
}

export default function MyProductsPage() {
  const navigate = useNavigate()
  const { role, companyId } = useAuth()
  const { products, loading, error, refetch } = useProducts()
  const { deleteProduct } = useProduct()

  // Redirect buyers away
  useEffect(() => {
    if (role === 'BUYER') navigate('/catalog', { replace: true })
  }, [role, navigate])

  const myProducts = useMemo(
    () => products.filter((p) => p.supplierId === companyId),
    [products, companyId]
  )

  async function handleDelete(id: number, name: string) {
    if (!window.confirm(`Delete "${name}"? This cannot be undone.`)) return
    try {
      await deleteProduct(id)
      refetch()
    } catch {
      // Error handled by apiClient (401/403 redirect)
    }
  }

  if (role === 'BUYER') return null

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-gray-400">Loading products...</div>
  }

  if (error) {
    return <div className="bg-red-50 text-red-600 p-4 rounded-lg">{error}</div>
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">My Products</h1>
        <button
          onClick={() => navigate('/my-products/new')}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700 transition-colors"
        >
          Add Product
        </button>
      </div>

      {myProducts.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8 text-center text-gray-400">
          No products yet. Click "Add Product" to list your first product.
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 bg-gray-50">
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Name</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">SKU</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Category</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Price</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Qty</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody>
                {myProducts.map((product) => (
                  <tr key={product.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{product.name}</td>
                    <td className="px-4 py-3 text-gray-500">{product.sku}</td>
                    <td className="px-4 py-3 text-gray-500">{product.category}</td>
                    <td className="px-4 py-3 text-right">
                      {product.currency} {Number(product.basePrice).toFixed(2)}
                    </td>
                    <td className="px-4 py-3 text-right">{product.availableQuantity}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[product.status]}`}>
                        {product.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => navigate(`/my-products/${product.id}/edit`)}
                        className="text-blue-600 hover:text-blue-800 text-xs font-medium mr-3"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(product.id, product.name)}
                        className="text-red-600 hover:text-red-800 text-xs font-medium"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
