export interface AuthResponse {
  token: string
  companyId: number
  email: string
  role: 'SUPPLIER' | 'BUYER'
}

export interface CompanyRequest {
  name: string
  email: string
  password: string
  role: 'SUPPLIER' | 'BUYER'
  contactPhone?: string
  address?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface CompanyResponse {
  id: number
  name: string
  email: string
  role: 'SUPPLIER' | 'BUYER'
  contactPhone: string | null
  address: string | null
  verified: boolean
  createdAt: string
}

export interface ConversationResponse {
  id: number
  type: 'DIRECT' | 'RFQ' | 'ORDER'
  referenceId: number | null
  subject: string | null
  participants: ParticipantInfo[]
  lastMessagePreview: string | null
  lastMessageSenderName: string | null
  lastMessageAt: string | null
  unreadCount: number
  createdAt: string
}

export interface ParticipantInfo {
  companyId: number
  companyName: string
}

export interface MessageResponse {
  id: number
  conversationId: number
  senderCompanyId: number
  senderCompanyName: string
  content: string
  createdAt: string
}

export interface SendMessageRequest {
  conversationId?: number
  referenceType?: 'DIRECT' | 'RFQ' | 'ORDER'
  referenceId?: number
  recipientCompanyId?: number
  subject?: string
  content: string
}

export interface NotificationResponse {
  id: number
  recipientId: number
  type: string
  message: string
  referenceId: number | null
  referenceType: string | null
  read: boolean
  createdAt: string
}

export interface SupplierAnalytics {
  totalProducts: number
  totalRevenue: number
  totalOrdersReceived: number
  averageOrderValue: number
  negotiationSuccessRate: number
  totalRfqs: number
  revenueByProduct: ProductRevenue[]
  ordersByStatus: Record<string, number>
}

export interface BuyerAnalytics {
  totalOrdersPlaced: number
  totalSpend: number
  averageOrderValue: number
  rfqSuccessRate: number
  totalRfqs: number
  ordersByStatus: Record<string, number>
}

export interface ProductRevenue {
  productId: number
  productName: string
  revenue: number
  orderCount: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export type ProductStatus = 'ACTIVE' | 'OUT_OF_STOCK' | 'DISCONTINUED'

export interface ProductResponse {
  id: number
  supplierId: number
  supplierName: string
  name: string
  description: string | null
  category: string
  sku: string
  unitOfMeasure: string
  basePrice: number
  availableQuantity: number
  status: ProductStatus
  currency: string
  createdAt: string
}

export interface ProductRequest {
  name: string
  description?: string
  category: string
  sku: string
  unitOfMeasure: string
  basePrice: number
  availableQuantity: number
  currency?: string
}

export interface ProductSearchParams {
  name?: string
  category?: string
  minPrice?: number
  maxPrice?: number
  status?: ProductStatus
}

export type OrderStatus = 'PLACED' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'

export interface OrderResponse {
  id: number
  buyerId: number
  buyerName: string
  productId: number
  productName: string
  supplierId: number
  supplierName: string
  quantity: number
  unitPrice: number
  totalPrice: number
  status: OrderStatus
  currency: string
  createdAt: string
}
