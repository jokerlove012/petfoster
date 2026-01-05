import api from './index'
import type { ApiResponse } from '@/types/common'

export const healthApi = {
  // 获取订单的健康记录
  getByBookingId(bookingId: string): Promise<ApiResponse<any[]>> {
    return api.get(`/health-records/booking/${bookingId}`)
  },

  // 创建健康记录
  create(bookingId: string, data: any): Promise<ApiResponse<any>> {
    return api.post(`/health-records/booking/${bookingId}`, data)
  }
}
