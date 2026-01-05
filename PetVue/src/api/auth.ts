import api from './index'
import type { ApiResponse } from '@/types/common'
import type { User, RegisterData, AuthResult, UserRole } from '@/types/user'

// 登录凭证接口（使用手机号和角色）
export interface LoginCredentials {
  phone: string
  password: string
  role: UserRole
}

// 用户资料更新接口
export interface ProfileUpdateData {
  name?: string
  nickname?: string
  email?: string
  avatar?: string
}

export const authApi = {
  // 登录（手机号 + 密码 + 角色）
  login(credentials: LoginCredentials): Promise<ApiResponse<AuthResult>> {
    return api.post('/auth/login', credentials)
  },

  // 注册
  register(data: RegisterData): Promise<ApiResponse<{ user: User }>> {
    return api.post('/auth/register', data)
  },

  // 登出
  logout(): Promise<ApiResponse<null>> {
    return api.post('/auth/logout')
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<ApiResponse<User>> {
    return api.get('/auth/me')
  },

  // 获取用户资料
  getProfile(): Promise<ApiResponse<User>> {
    return api.get('/user/profile')
  },

  // 更新用户资料
  updateProfile(data: ProfileUpdateData): Promise<ApiResponse<User>> {
    return api.put('/user/profile', data)
  },

  // 刷新 Token
  refreshToken(): Promise<ApiResponse<{ token: string }>> {
    return api.post('/auth/refresh')
  },

  // 修改密码
  changePassword(oldPassword: string, newPassword: string): Promise<ApiResponse<null>> {
    return api.post('/auth/change-password', { oldPassword, newPassword })
  }
}
