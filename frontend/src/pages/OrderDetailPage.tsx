import { useParams, useNavigate, Link } from 'react-router-dom'
import { useOrder } from '../hooks/useOrder'
import { useAuth } from '../contexts/AuthContext'
import type { OrderStatus } from '../api/types'

const STATUS_COLORS: Record<OrderStatus, string> = {
  PLACED: 'bg-blue-100 text-blue-800',
  CONFIRMED: 'bg-indigo-100 text-indigo-800',
  SHIPPED: 'bg-yellow-100 text-yellow-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { role, companyId } = useAuth()
  const { order, loading, error, saving, updateStatus } = useOrder(Number(id))

  const isSupplier = role === 'SUPPLIER' && order?.supplierId === companyId

  async function handleStatusUpdate(status: OrderStatus) {
    if (!order) return
    try {
      await updateStatus(order.id, status)
    } catch {
      // Error handled by apiClient
    }
  }

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-gray-400">Loading order...</div>
  }

  if (error) {
    return <div className="bg-red-50 text-red-600 p-4 rounded-lg">{error}</div>
  }

  if (!order) {
    return <div className="text-center text-gray-400 py-16">Order not found</div>
  }

  return (
    <div>
      <button
        onClick={() => navigate('/orders')}
        className="text-sm text-gray-500 hover:text-gray-700 mb-4 flex items-center gap-1"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Back to Orders
      </button>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="text-2xl font-bold">Order #{order.id}</h1>
            <p className="text-gray-500 mt-1">
              Placed {new Date(order.createdAt).toLocaleDateString()}
            </p>
          </div>
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${STATUS_COLORS[order.status]}`}>
            {order.status}
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-4">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Product</p>
              <Link
                to={`/catalog/${order.productId}`}
                className="text-sm font-medium text-blue-600 hover:text-blue-800"
              >
                {order.productName}
              </Link>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Buyer</p>
              <p className="text-sm font-medium">{order.buyerName}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Supplier</p>
              <p className="text-sm font-medium">{order.supplierName}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Order Date</p>
              <p className="text-sm font-medium">{new Date(order.createdAt).toLocaleDateString()}</p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Unit Price</p>
              <p className="text-sm font-medium">
                {order.currency} {Number(order.unitPrice).toFixed(2)}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Quantity</p>
              <p className="text-sm font-medium">{order.quantity}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Total Price</p>
              <p className="text-2xl font-bold text-blue-600">
                {order.currency} {Number(order.totalPrice).toFixed(2)}
              </p>
            </div>
          </div>
        </div>

        {isSupplier && (
          <div className="mt-6 pt-6 border-t border-gray-200 flex gap-3">
            {order.status === 'PLACED' && (
              <>
                <button
                  onClick={() => handleStatusUpdate('CONFIRMED')}
                  disabled={saving}
                  className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700 transition-colors disabled:opacity-50"
                >
                  {saving ? 'Updating...' : 'Confirm'}
                </button>
                <button
                  onClick={() => handleStatusUpdate('CANCELLED')}
                  disabled={saving}
                  className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700 transition-colors disabled:opacity-50"
                >
                  Cancel
                </button>
              </>
            )}
            {order.status === 'CONFIRMED' && (
              <>
                <button
                  onClick={() => handleStatusUpdate('SHIPPED')}
                  disabled={saving}
                  className="bg-yellow-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-yellow-700 transition-colors disabled:opacity-50"
                >
                  {saving ? 'Updating...' : 'Ship'}
                </button>
                <button
                  onClick={() => handleStatusUpdate('CANCELLED')}
                  disabled={saving}
                  className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700 transition-colors disabled:opacity-50"
                >
                  Cancel
                </button>
              </>
            )}
            {order.status === 'SHIPPED' && (
              <button
                onClick={() => handleStatusUpdate('DELIVERED')}
                disabled={saving}
                className="bg-green-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-green-700 transition-colors disabled:opacity-50"
              >
                {saving ? 'Updating...' : 'Mark Delivered'}
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
