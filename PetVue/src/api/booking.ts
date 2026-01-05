import api from './index'
import type { ApiResponse, PaginatedData } from '@/types/common'
import type { Booking, CreateBookingData, BookingStatus, PaymentMethod } from '@/types/booking'

export interface CancelResult {
  booking: Booking
  refundAmount: number
}

export const bookingApi = {
  // 创建预约
  create(data: CreateBookingData): Promise<ApiResponse<Booking>> {
    return api.post('/bookings', data)
  },

  // 获取订单列表
  getList(params?: { status?: BookingStatus; page?: number; pageSize?: number }): Promise<ApiResponse<PaginatedData<Booking>>> {
    const searchParams = new URLSearchParams()
    if (params?.status) searchParams.append('status', params.status)
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.pageSize) searchParams.append('pageSize', params.pageSize.toString())
    
    return api.get(`/bookings?${searchParams.toString()}`)
  },

  // 获取订单详情
  getDetail(id: string): Promise<ApiResponse<Booking>> {
    return api.get(`/bookings/${id}`)
  },

  // 取消订单
  cancel(id: string, reason?: string): Promise<ApiResponse<CancelResult>> {
    return api.post(`/bookings/${id}/cancel`, { reason })
  },

  // 支付订单
  pay(id: string, paymentMethod: PaymentMethod): Promise<ApiResponse<Booking>> {
    return api.post(`/bookings/${id}/pay`, { paymentMethod })
  },

  // 机构确认订单
  confirm(id: string): Promise<ApiResponse<Booking>> {
    return api.post(`/bookings/${id}/confirm`)
  },

  // 机构拒绝订单
  reject(id: string, reason: string): Promise<ApiResponse<Booking>> {
    return api.post(`/bookings/${id}/reject`, { reason })
  },

  // 办理入住
  checkIn(id: string): Promise<ApiResponse<Booking>> {
    return api.post(`/bookings/${id}/check-in`)
  },

  // 办理退房
  checkOut(id: string): Promise<ApiResponse<Booking>> {
    return api.post(`/bookings/${id}/check-out`)
  },

  // 删除订单
  delete(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/bookings/${id}`)
  }
}
