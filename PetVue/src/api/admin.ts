import api from './index'
import type { ApiResponse, PaginatedData } from '@/types/common'

export const adminApi = {
  // 获取仪表盘统计
  getDashboardStats(period = 'month', startDate?: string, endDate?: string): Promise<ApiResponse<any>> {
    const params: any = { period }
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    return api.get('/admin/dashboard/stats', { params })
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

  // 获取用户详情
  getUserDetail(id: string): Promise<ApiResponse<any>> {
    return api.get(`/admin/users/${id}`)
  },

  // 创建用户
  createUser(data: any): Promise<ApiResponse<any>> {
    return api.post('/admin/users', data)
  },

  // 更新用户
  updateUser(id: string, data: any): Promise<ApiResponse<any>> {
    return api.put(`/admin/users/${id}`, data)
  },

  // 禁用/启用用户
  toggleUserStatus(id: string, status: string): Promise<ApiResponse<any>> {
    return api.patch(`/admin/users/${id}/status`, { status })
  },

  // 删除用户
  deleteUser(id: string): Promise<ApiResponse<any>> {
    return api.delete(`/admin/users/${id}`)
  },

  // 获取订单列表
  getOrders(status?: string, page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    const params = new URLSearchParams()
    if (status) params.append('status', status)
    params.append('page', page.toString())
    params.append('pageSize', pageSize.toString())
    return api.get(`/admin/orders?${params.toString()}`)
  },

  // 获取订单详情
  getOrderDetail(id: string): Promise<ApiResponse<any>> {
    return api.get(`/admin/orders/${id}`)
  },

  // ========== 退款管理 ==========

  // 获取退款列表
  getRefunds(status?: string, page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    const params = new URLSearchParams()
    if (status) params.append('status', status)
    params.append('page', page.toString())
    params.append('pageSize', pageSize.toString())
    return api.get(`/admin/refunds?${params.toString()}`)
  },

  // 获取退款统计
  getRefundStats(): Promise<ApiResponse<any>> {
    return api.get('/admin/refunds/stats')
  },

  // 获取退款详情
  getRefundDetail(id: string): Promise<ApiResponse<any>> {
    return api.get(`/admin/refunds/${id}`)
  },

  // 批准退款
  approveRefund(id: string): Promise<ApiResponse<any>> {
    return api.post(`/admin/refunds/${id}/approve`)
  },

  // 拒绝退款
  rejectRefund(id: string, reason: string): Promise<ApiResponse<any>> {
    return api.post(`/admin/refunds/${id}/reject`, { reason })
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
  },

  // ========== 财务管理 ==========
  
  // 获取财务概览
  getFinanceSummary(period = 'month'): Promise<ApiResponse<any>> {
    return api.get('/admin/finance/summary', { params: { period } })
  },

  // 获取财务趋势
  getFinanceTrends(period = 'month'): Promise<ApiResponse<any[]>> {
    return api.get('/admin/finance/trends', { params: { period } })
  },

  // 获取机构收入排名
  getInstitutionRanking(): Promise<ApiResponse<any[]>> {
    return api.get('/admin/finance/institutions/ranking')
  },

  // 获取交易记录
  getTransactions(type?: string, page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    const params = new URLSearchParams()
    if (type) params.append('type', type)
    params.append('page', page.toString())
    params.append('pageSize', pageSize.toString())
    return api.get(`/admin/finance/transactions?${params.toString()}`)
  },

  // 获取待结算机构
  getPendingSettlements(): Promise<ApiResponse<any[]>> {
    return api.get('/admin/finance/settlements')
  },

  // 处理结算
  processSettlement(id: string): Promise<ApiResponse<any>> {
    return api.post(`/admin/finance/settlements/${id}/process`)
  }
}
