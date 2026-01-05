import api from './index'
import type { ApiResponse } from './index'

export interface Notification {
  id: string
  userId: string
  type: string
  title: string
  content: string
  isRead: boolean
  link?: string
  createdAt: string
}

export const notificationApi = {
  // 获取通知列表
  list(params?: { type?: string; isRead?: boolean }): Promise<ApiResponse<Notification[]>> {
    return api.get('/notifications', { params })
  },

  // 获取未读数量
  getUnreadCount(): Promise<ApiResponse<{ count: number }>> {
    return api.get('/notifications/unread-count')
  },

  // 标记为已读
  markAsRead(id: string): Promise<ApiResponse<void>> {
    return api.put(`/notifications/${id}/read`)
  },

  // 标记全部已读
  markAllAsRead(): Promise<ApiResponse<void>> {
    return api.put('/notifications/read-all')
  },

  // 删除通知
  delete(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/notifications/${id}`)
  },

  // 清空已读通知
  clearRead(): Promise<ApiResponse<void>> {
    return api.delete('/notifications/clear-read')
  }
}
