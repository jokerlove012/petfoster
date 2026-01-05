import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserRole } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const userRole = computed<UserRole | null>(() => user.value?.role ?? null)

  // Actions
  function setUser(userData: User) {
    user.value = userData
    // 使用 sessionStorage，关闭标签页后自动清除
    sessionStorage.setItem('user', JSON.stringify(userData))
  }

  function setToken(tokenValue: string) {
    token.value = tokenValue
    sessionStorage.setItem('token', tokenValue)
  }

  function logout() {
    user.value = null
    token.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    // 同时清除 localStorage 中的旧数据
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function initFromStorage() {
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
  }

  return {
    user,
    token,
    isAuthenticated,
    userRole,
    setUser,
    setToken,
    logout,
    initFromStorage,
  }
})
