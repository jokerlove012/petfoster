import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserRole } from '@/types/user'
import { authApi } from '@/api/auth'

/**
 * 认证状态管理 Store
 * 
 * 负责管理用户登录状态、token 令牌和用户信息
 * 使用 Pinia 进行状态管理，数据持久化到 sessionStorage
 */
export const useAuthStore = defineStore('auth', () => {
  // ==========================================
  // 状态定义 (State)
  // ==========================================
  
  /** 当前登录用户信息，未登录时为 null */
  const user = ref<User | null>(null)
  
  /** JWT 认证令牌，用于 API 请求鉴权 */
  const token = ref<string | null>(null)
  
  /** 是否正在加载用户数据的标识 */
  const isLoading = ref(false)

  // ==========================================
  // 计算属性 (Getters)
  // ==========================================
  
  /** 判断用户是否已登录（同时检查 token 和 user 都存在） */
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  
  /** 获取当前用户的角色类型 */
  const userRole = computed<UserRole | null>(() => user.value?.role ?? null)

  // ==========================================
  // 动作方法 (Actions)
  // ==========================================
  
  /**
   * 设置用户信息
   * 同时更新内存状态和 sessionStorage 持久化
   * @param userData - 用户对象
   */
  function setUser(userData: User) {
    user.value = userData
    sessionStorage.setItem('user', JSON.stringify(userData))
  }

  /**
   * 设置认证令牌
   * 同时更新内存状态和 sessionStorage 持久化
   * @param tokenValue - JWT Token 字符串
   */
  function setToken(tokenValue: string) {
    token.value = tokenValue
    sessionStorage.setItem('token', tokenValue)
  }

  /**
   * 从后端获取最新的用户信息
   * 用于页面刷新后同步最新数据，确保头像等信息是最新的
   */
  async function fetchUser() {
    if (!token.value) return
    
    try {
      isLoading.value = true
      const res = await authApi.getCurrentUser()
      if (res.code === 200 && res.data) {
        setUser(res.data)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 用户退出登录
   * 清空所有状态和存储的数据
   */
  function logout() {
    user.value = null
    token.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  /**
   * 从存储中初始化认证状态
   * 应用启动时调用，恢复之前的登录状态并获取最新用户信息
   */
  async function initFromStorage() {
    const storedToken = sessionStorage.getItem('token')
    const storedUser = sessionStorage.getItem('user')
    
    if (storedToken) {
      token.value = storedToken
    }
    
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch {
        sessionStorage.removeItem('user')
      }
    }
    
    if (token.value) {
      await fetchUser()
    }
  }

  // ==========================================
  // 导出状态和方法
  // ==========================================
  return {
    user,
    token,
    isAuthenticated,
    userRole,
    isLoading,
    setUser,
    setToken,
    fetchUser,
    logout,
    initFromStorage,
  }
})
