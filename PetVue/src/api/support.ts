import api from './index'
import type { ApiResponse, PaginatedData } from '@/types/common'

export interface ComplaintSubmitData {
  category: string
  subject: string
  description: string
  bookingId?: string
  attachments?: string[]
  contactPhone?: string
  contactEmail?: string
}

export const supportApi = {
  // 提交投诉/工单
  submitComplaint(data: ComplaintSubmitData): Promise<ApiResponse<{ id: string; complaintNumber: string; status: string }>> {
    return api.post('/user/complaints', data)
  },

  // 获取用户的投诉列表
  getMyComplaints(page = 1, pageSize = 10): Promise<ApiResponse<PaginatedData<any>>> {
    return api.get(`/user/complaints?page=${page}&pageSize=${pageSize}`)
  }
}
