import { Link } from 'react-router-dom'
import { useDashboard } from '../hooks/useDashboard'

function StatCard({ label, value, sub, to }: { label: string; value: string | number; sub?: string; to?: string }) {
  const content = (
    <div className={`bg-white rounded-xl shadow-sm border border-gray-200 p-6${to ? ' hover:border-blue-300 transition-colors' : ''}`}>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-2xl font-bold mt-1">{value}</p>
      {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
    </div>
  )
  if (to) return <Link to={to}>{content}</Link>
  return content
}

function OrderStatusTable({ data }: { data: Record<string, number> }) {
  const entries = Object.entries(data)
  if (entries.length === 0) return <p className="text-gray-400 text-sm">No orders yet</p>

  return (
    <div className="space-y-2">
      {entries.map(([status, count]) => (
        <Link
          key={status}
          to={`/orders?status=${status}`}
          className="flex justify-between items-center hover:bg-gray-50 rounded px-1 -mx-1 py-0.5 transition-colors"
        >
          <span className="text-sm text-gray-600">{status.replace(/_/g, ' ')}</span>
          <span className="text-sm font-medium bg-gray-100 px-2 py-0.5 rounded">{count}</span>
        </Link>
      ))}
    </div>
  )
}

export default function DashboardPage() {
  const { supplierData, buyerData, unreadMessages, loading, error, role } = useDashboard()

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-400">Loading dashboard...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 text-red-600 p-4 rounded-lg">{error}</div>
    )
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>

      {role === 'SUPPLIER' && supplierData && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard label="Total Products" value={supplierData.totalProducts} />
            <StatCard
              label="Total Revenue"
              value={`$${(supplierData.totalRevenue ?? 0).toLocaleString()}`}
            />
            <StatCard label="Orders Received" value={supplierData.totalOrdersReceived} to="/orders" />
            <StatCard
              label="Avg Order Value"
              value={`$${(supplierData.averageOrderValue ?? 0).toFixed(2)}`}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h3 className="font-semibold mb-4">Negotiation Success Rate</h3>
              <p className="text-3xl font-bold text-green-600">
                {(supplierData.negotiationSuccessRate ?? 0).toFixed(1)}%
              </p>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h3 className="font-semibold mb-4">Orders by Status</h3>
              <OrderStatusTable data={supplierData.ordersByStatus} />
            </div>

            <Link to="/messages" className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 block hover:border-blue-300 transition-colors">
              <h3 className="font-semibold mb-4">Unread Messages</h3>
              <p className="text-3xl font-bold text-blue-600">{unreadMessages}</p>
            </Link>

            {supplierData.revenueByProduct.length > 0 && (
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 md:col-span-2 lg:col-span-3">
                <h3 className="font-semibold mb-4">Top Revenue Products</h3>
                <div className="space-y-3">
                  {supplierData.revenueByProduct.slice(0, 5).map((p) => (
                    <div key={p.productId} className="flex justify-between items-center">
                      <span className="text-sm text-gray-700">{p.productName}</span>
                      <div className="text-sm text-right">
                        <span className="font-medium">${p.revenue.toLocaleString()}</span>
                        <span className="text-gray-400 ml-2">({p.orderCount} orders)</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </>
      )}

      {role === 'BUYER' && buyerData && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard label="Orders Placed" value={buyerData.totalOrdersPlaced} to="/orders" />
            <StatCard
              label="Total Spend"
              value={`$${(buyerData.totalSpend ?? 0).toLocaleString()}`}
            />
            <StatCard
              label="Avg Order Value"
              value={`$${(buyerData.averageOrderValue ?? 0).toFixed(2)}`}
            />
            <StatCard label="Unread Messages" value={unreadMessages} to="/messages" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h3 className="font-semibold mb-4">RFQ Success Rate</h3>
              <p className="text-3xl font-bold text-green-600">
                {(buyerData.rfqSuccessRate ?? 0).toFixed(1)}%
              </p>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h3 className="font-semibold mb-4">Orders by Status</h3>
              <OrderStatusTable data={buyerData.ordersByStatus} />
            </div>
          </div>
        </>
      )}
    </div>
  )
}
