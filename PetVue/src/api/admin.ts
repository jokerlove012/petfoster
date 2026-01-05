import api from './index'
import type { ApiResponse, PaginatedData } from '@/types/common'

export const adminApi = {
  // 获取仪表盘统计
  getDashboardStats(): Promise<ApiResponse<any>> {
    return api.get('/admin/dashboard/stats')
  },

  // 获取机构列表（审核）
  getInstitutions(status?: string, page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    const params = new URLSearchParams()
    if (status) params.append('status', status)
    params.append('page', page.toString())
    params.append('pageSize', pageSize.toString())
    return api.get(`/admin/institutions?${params.toString()}`)
  },

  // 获取机构详情
  getInstitutionDetail(id: string): Promise<ApiResponse<any>> {
    return api.get(`/admin/institutions/${id}`)
  },

  // 审核通过机构
  approveInstitution(id: string): Promise<ApiResponse<any>> {
    return api.post(`/admin/institutions/${id}/approve`)
  },

  // 拒绝机构
  rejectInstitution(id: string, reason: string): Promise<ApiResponse<any>> {
    return api.post(`/admin/institutions/${id}/reject`, { reason })
  },

  // 获取用户列表
  getUsers(role?: string, keyword?: string, page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    const params = new URLSearchParams()
    if (role) params.append('role', role)
    if (keyword) params.append('keyword', keyword)
    params.append('page', page.toString())
    params.append('pageSize', pageSize.toString())
    return api.get(`/admin/users?${params.toString()}`)
  },

  // 获取订单列表
  getOrders(status?: string, page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    const params = new URLSearchParams()
    if (status) params.append('status', status)
    params.append('page', page.toString())
    params.append('pageSize', pageSize.toString())
    return api.get(`/admin/orders?${params.toString()}`)
  },

  // ========== 投诉管理 ==========
  
  // 获取投诉列表
  getComplaints(status?: string, page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    const params = new URLSearchParams()
    params.append('status', status || 'all')
    params.append('page', page.toString())
    params.append('pageSize', pageSize.toString())
    return api.get(`/admin/complaints?${params.toString()}`)
  },

  // 获取投诉统计
  getComplaintStats(): Promise<ApiResponse<any>> {
    return api.get('/admin/complaints/stats')
  },

  // 获取投诉详情
  getComplaintDetail(id: string): Promise<ApiResponse<any>> {
    return api.get(`/admin/complaints/${id}`)
  },

  // 催促机构回复
  urgeComplaintResponse(id: string): Promise<ApiResponse<any>> {
    return api.post(`/admin/complaints/${id}/urge`)
  },

  // 处理投诉
  resolveComplaint(id: string, decision: string, remedies?: string): Promise<ApiResponse<any>> {
    return api.post(`/admin/complaints/${id}/resolve`, { decision, remedies })
  }
}
