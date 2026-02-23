import { useMemo } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useOrders } from '../hooks/useOrders'
import { useAuth } from '../contexts/AuthContext'
import type { OrderStatus } from '../api/types'

const STATUS_COLORS: Record<OrderStatus, string> = {
  PLACED: 'bg-blue-100 text-blue-800',
  CONFIRMED: 'bg-indigo-100 text-indigo-800',
  SHIPPED: 'bg-yellow-100 text-yellow-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

const STATUSES: ('ALL' | OrderStatus)[] = ['ALL', 'PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED']

export default function OrdersPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const { role } = useAuth()
  const { orders, loading, error } = useOrders()

  const activeFilter = (searchParams.get('status') as OrderStatus | null) ?? 'ALL'

  const filteredOrders = useMemo(() => {
    if (activeFilter === 'ALL') return orders
    return orders.filter((o) => o.status === activeFilter)
  }, [orders, activeFilter])

  function setFilter(status: 'ALL' | OrderStatus) {
    if (status === 'ALL') {
      setSearchParams({})
    } else {
      setSearchParams({ status })
    }
  }

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-gray-400">Loading orders...</div>
  }

  if (error) {
    return <div className="bg-red-50 text-red-600 p-4 rounded-lg">{error}</div>
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Orders</h1>

      <div className="flex gap-2 mb-6 flex-wrap">
        {STATUSES.map((status) => (
          <button
            key={status}
            onClick={() => setFilter(status)}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              activeFilter === status
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {status}
          </button>
        ))}
      </div>

      {filteredOrders.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8 text-center text-gray-400">
          {activeFilter === 'ALL' ? 'No orders yet.' : `No ${activeFilter} orders.`}
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 bg-gray-50">
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Order #</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Product</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Counterparty</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Qty</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Unit Price</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Total</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Date</th>
                </tr>
              </thead>
              <tbody>
                {filteredOrders.map((order) => (
                  <tr
                    key={order.id}
                    onClick={() => navigate(`/orders/${order.id}`)}
                    className="border-b border-gray-100 hover:bg-gray-50 cursor-pointer"
                  >
                    <td className="px-4 py-3 font-medium">#{order.id}</td>
                    <td className="px-4 py-3">{order.productName}</td>
                    <td className="px-4 py-3 text-gray-500">
                      {role === 'BUYER' ? order.supplierName : order.buyerName}
                    </td>
                    <td className="px-4 py-3 text-right">{order.quantity}</td>
                    <td className="px-4 py-3 text-right">
                      {order.currency} {Number(order.unitPrice).toFixed(2)}
                    </td>
                    <td className="px-4 py-3 text-right font-medium">
                      {order.currency} {Number(order.totalPrice).toFixed(2)}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[order.status]}`}>
                        {order.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-500">
                      {new Date(order.createdAt).toLocaleDateString()}
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
