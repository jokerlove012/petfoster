export type BookingStatus = 'pending' | 'confirmed' | 'in_progress' | 'completed' | 'cancelled'
export type PaymentStatus = 'pending' | 'paid' | 'refunded' | 'partial_refund'
export type PaymentMethod = 'wechat' | 'alipay' | 'wallet'

export interface EmergencyContact {
  name: string
  phone: string
  relationship: string
}

export interface Booking {
  id: string
  orderNumber: string
  userId: string
  institutionId: string
  servicePackageId: string
  petId: string
  status: BookingStatus
  startDate: string
  endDate: string
  checkInTime?: string
  checkOutTime?: string
  totalDays: number
  basePrice: number
  discount: number
  totalPrice: number
  paymentStatus: PaymentStatus
  paymentMethod?: PaymentMethod
  paidAt?: string
  refundAmount?: number
  refundedAt?: string
  cancelReason?: string
  specialRequirements?: string
  emergencyContact: EmergencyContact
  createdAt: string
  updatedAt: string
}

export interface CreateBookingData {
  institutionId: string
  servicePackageId: string
  petId: string
  startDate: string
  endDate: string
  specialRequirements?: string
  emergencyContact: EmergencyContact
}

export interface BookingListParams {
  status?: BookingStatus
  page?: number
  pageSize?: number
}
