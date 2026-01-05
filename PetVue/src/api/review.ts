import api from './index'
import type { ApiResponse, PaginatedData } from '@/types/common'
import type { Review, CreateReviewData } from '@/types/review'

export const reviewApi = {
  // 创建评价
  create(data: CreateReviewData): Promise<ApiResponse<Review>> {
    return api.post('/reviews', data)
  },

  // 获取评价详情
  getDetail(id: string): Promise<ApiResponse<Review>> {
    return api.get(`/reviews/${id}`)
  },

  // 获取用户的评价列表
  getUserReviews(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PaginatedData<Review>>> {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.pageSize) searchParams.append('pageSize', params.pageSize.toString())
    
    return api.get(`/user/reviews?${searchParams.toString()}`)
  },

  // 机构回复评价
  reply(id: string, content: string): Promise<ApiResponse<Review>> {
    return api.post(`/reviews/${id}/reply`, { content })
  },

  // 删除评价
  delete(id: string): Promise<ApiResponse<null>> {
    return api.delete(`/reviews/${id}`)
  }
}
