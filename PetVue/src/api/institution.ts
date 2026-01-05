import api from './index'
import type { ApiResponse, PaginatedData } from '@/types/common'
import type { Institution, InstitutionWithDistance, ServicePackage, SearchFilters } from '@/types/institution'
import type { Review } from '@/types/review'

export interface InstitutionDetail extends Institution {
  servicePackages: ServicePackage[]
  recentReviews: Review[]
}

export interface AvailabilityResult {
  available: boolean
  remainingSlots: number
  startDate: string
  endDate: string
}

export const institutionApi = {
  // 搜索机构列表
  search(filters: SearchFilters & { page?: number; pageSize?: number }): Promise<ApiResponse<PaginatedData<InstitutionWithDistance>>> {
    const params = new URLSearchParams()
    if (filters.keyword) params.append('keyword', filters.keyword)
    if (filters.petType) params.append('petType', filters.petType)
    if (filters.minRating) params.append('minRating', filters.minRating.toString())
    if (filters.maxPrice) params.append('maxPrice', filters.maxPrice.toString())
    if (filters.sortBy) params.append('sortBy', filters.sortBy)
    if (filters.lat) params.append('lat', filters.lat.toString())
    if (filters.lng) params.append('lng', filters.lng.toString())
    if (filters.page) params.append('page', filters.page.toString())
    if (filters.pageSize) params.append('pageSize', filters.pageSize.toString())
    
    return api.get(`/institutions?${params.toString()}`)
  },

  // 获取机构详情
  getDetail(id: string): Promise<ApiResponse<InstitutionDetail>> {
    return api.get(`/institutions/${id}`)
  },

  // 获取机构服务套餐
  getPackages(id: string): Promise<ApiResponse<ServicePackage[]>> {
    return api.get(`/institutions/${id}/packages`)
  },

  // 获取机构评价列表
  getReviews(id: string, params?: { page?: number; pageSize?: number; sortBy?: string }): Promise<ApiResponse<PaginatedData<Review>>> {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.pageSize) searchParams.append('pageSize', params.pageSize.toString())
    if (params?.sortBy) searchParams.append('sortBy', params.sortBy)
    
    return api.get(`/institutions/${id}/reviews?${searchParams.toString()}`)
  },

  // 检查机构可用性
  checkAvailability(id: string, startDate: string, endDate: string, petType: string): Promise<ApiResponse<AvailabilityResult>> {
    return api.get(`/institutions/${id}/availability`, {
      params: { startDate, endDate, petType }
    })
  },

  // 收藏机构
  addFavorite(id: string): Promise<ApiResponse<null>> {
    return api.post(`/institutions/${id}/favorite`)
  },

  // 取消收藏
  removeFavorite(id: string): Promise<ApiResponse<null>> {
    return api.delete(`/institutions/${id}/favorite`)
  },

  // 获取收藏列表
  getFavorites(page?: number, pageSize?: number): Promise<ApiResponse<PaginatedData<Institution>>> {
    const params = new URLSearchParams()
    if (page) params.append('page', page.toString())
    if (pageSize) params.append('pageSize', pageSize.toString())
    
    return api.get(`/user/favorites?${params.toString()}`)
  }
}

// 机构管理端API
export const institutionManageApi = {
  // 获取仪表盘统计数据
  getDashboardStats(): Promise<ApiResponse<any>> {
    return api.get('/institution/dashboard/stats')
  },

  // 获取最近订单
  getRecentOrders(limit = 10): Promise<ApiResponse<any[]>> {
    return api.get(`/institution/dashboard/recent-orders?limit=${limit}`)
  },

  // 获取今日入住
  getTodayCheckIn(): Promise<ApiResponse<any[]>> {
    return api.get('/institution/dashboard/today-checkin')
  },

  // 获取今日离店
  getTodayCheckOut(): Promise<ApiResponse<any[]>> {
    return api.get('/institution/dashboard/today-checkout')
  },

  // 获取客户列表
  getCustomers(page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    return api.get(`/institution/customers?page=${page}&pageSize=${pageSize}`)
  },

  // 获取订单列表
  getBookings(page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    return api.get(`/institution/bookings?page=${page}&pageSize=${pageSize}`)
  },

  // 确认订单
  confirmBooking(id: string): Promise<ApiResponse<any>> {
    return api.post(`/institution/bookings/${id}/confirm`)
  },

  // 拒绝订单
  rejectBooking(id: string, reason: string): Promise<ApiResponse<any>> {
    return api.post(`/institution/bookings/${id}/reject`, { reason })
  },

  // 办理入住
  checkIn(id: string): Promise<ApiResponse<any>> {
    return api.post(`/institution/bookings/${id}/check-in`)
  },

  // 办理离店
  checkOut(id: string): Promise<ApiResponse<any>> {
    return api.post(`/institution/bookings/${id}/check-out`)
  },

  // 获取服务套餐
  getPackages(): Promise<ApiResponse<any[]>> {
    return api.get('/institution/packages')
  },

  // 创建套餐
  createPackage(data: any): Promise<ApiResponse<any>> {
    return api.post('/institution/packages', data)
  },

  // 更新套餐
  updatePackage(id: string, data: any): Promise<ApiResponse<any>> {
    return api.put(`/institution/packages/${id}`, data)
  },

  // 删除套餐
  deletePackage(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/institution/packages/${id}`)
  },

  // 获取评价列表
  getReviews(page = 1, pageSize = 20): Promise<ApiResponse<PaginatedData<any>>> {
    return api.get(`/institution/reviews?page=${page}&pageSize=${pageSize}`)
  },

  // 回复评价
  replyReview(id: string, content: string): Promise<ApiResponse<any>> {
    return api.post(`/institution/reviews/${id}/reply`, { content })
  },

  // 获取机构设置
  getSettings(): Promise<ApiResponse<any>> {
    return api.get('/institution/settings')
  },

  // 更新机构设置
  updateSettings(data: any): Promise<ApiResponse<any>> {
    return api.put('/institution/settings', data)
  },

  // 获取机构资料（包含审核状态）
  getProfile(): Promise<ApiResponse<any>> {
    return api.get('/institution/profile')
  },

  // 申请创建机构
  applyInstitution(data: any): Promise<ApiResponse<any>> {
    return api.post('/institution/apply', data)
  },

  // 获取报表数据
  getReport(period = 'month'): Promise<ApiResponse<any>> {
    return api.get(`/institution/report?period=${period}`)
  },

  // 删除订单
  deleteBooking(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/institution/bookings/${id}`)
  }
}
