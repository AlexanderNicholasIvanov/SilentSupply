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
  avgOrderValue: number
  negotiationSuccessRate: number
  revenueByProduct: ProductRevenue[]
  ordersByStatus: Record<string, number>
}

export interface BuyerAnalytics {
  totalOrdersPlaced: number
  totalSpend: number
  avgOrderValue: number
  rfqSuccessRate: number
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
