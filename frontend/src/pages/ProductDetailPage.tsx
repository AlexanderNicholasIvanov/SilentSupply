import { useParams, useNavigate } from 'react-router-dom'
import { useProduct } from '../hooks/useProduct'
import { useAuth } from '../contexts/AuthContext'
import type { ProductStatus } from '../api/types'

const STATUS_COLORS: Record<ProductStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-800',
  OUT_OF_STOCK: 'bg-yellow-100 text-yellow-800',
  DISCONTINUED: 'bg-red-100 text-red-800',
}

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { companyId } = useAuth()
  const { product, loading, error } = useProduct(Number(id))

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-gray-400">Loading product...</div>
  }

  if (error) {
    return <div className="bg-red-50 text-red-600 p-4 rounded-lg">{error}</div>
  }

  if (!product) {
    return <div className="text-center text-gray-400 py-16">Product not found</div>
  }

  const isOwnProduct = product.supplierId === companyId

  function handleContactSupplier() {
    navigate('/messages', {
      state: {
        recipientId: product!.supplierId,
        subject: `Inquiry about ${product!.name}`,
      },
    })
  }

  return (
    <div>
      <button
        onClick={() => navigate('/catalog')}
        className="text-sm text-gray-500 hover:text-gray-700 mb-4 flex items-center gap-1"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Back to Catalog
      </button>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="text-2xl font-bold">{product.name}</h1>
            <p className="text-gray-500 mt-1">{product.supplierName}</p>
          </div>
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${STATUS_COLORS[product.status]}`}>
            {product.status.replace('_', ' ')}
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-4">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Category</p>
              <p className="text-sm font-medium">{product.category}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">SKU</p>
              <p className="text-sm font-medium">{product.sku}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Unit of Measure</p>
              <p className="text-sm font-medium">{product.unitOfMeasure}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Listed Date</p>
              <p className="text-sm font-medium">{new Date(product.createdAt).toLocaleDateString()}</p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Price</p>
              <p className="text-2xl font-bold text-blue-600">
                {product.currency} {Number(product.basePrice).toFixed(2)}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Available Quantity</p>
              <p className="text-sm font-medium">{product.availableQuantity}</p>
            </div>
            {product.description && (
              <div>
                <p className="text-xs text-gray-400 uppercase tracking-wide">Description</p>
                <p className="text-sm text-gray-700 whitespace-pre-wrap">{product.description}</p>
              </div>
            )}
          </div>
        </div>

        {!isOwnProduct && (
          <div className="mt-6 pt-6 border-t border-gray-200">
            <button
              onClick={handleContactSupplier}
              className="bg-blue-600 text-white px-6 py-2 rounded-lg text-sm hover:bg-blue-700 transition-colors"
            >
              Contact Supplier
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
