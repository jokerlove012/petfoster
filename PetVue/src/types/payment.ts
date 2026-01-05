export interface PaymentTransaction {
  id: string
  bookingId: string
  amount: number
  method: PaymentMethod
  status: TransactionStatus
  transactionId?: string
  createdAt: Date
}

export type PaymentMethod = 'credit_card' | 'debit_card' | 'alipay' | 'wechat_pay'
export type TransactionStatus = 'pending' | 'success' | 'failed' | 'refunded'

export interface RefundRequest {
  id: string
  bookingId: string
  amount: number
  reason: string
  status: RefundStatus
  createdAt: Date
  processedAt?: Date
}

export type RefundStatus = 'pending' | 'approved' | 'rejected' | 'completed'
