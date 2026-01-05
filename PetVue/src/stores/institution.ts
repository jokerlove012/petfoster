import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { InstitutionWithDistance, SearchFilters } from '@/types/institution'

const FAVORITES_KEY = 'pet_foster_favorites'
const HISTORY_KEY = 'pet_foster_history'
const MAX_HISTORY = 20

export const useInstitutionStore = defineStore('institution', () => {
  // State
  const institutions = ref<InstitutionWithDistance[]>([])
  const currentInstitution = ref<InstitutionWithDistance | null>(null)
  const filters = ref<SearchFilters>({
    sortBy: 'rating'
  })
  const loading = ref(false)
  const favorites = ref<string[]>([])
  const browsingHistory = ref<{ id: string; visitedAt: string }[]>([])

  // Getters
  const favoriteCount = computed(() => favorites.value.length)
  const historyCount = computed(() => browsingHistory.value.length)
  
  const isFavorite = computed(() => (id: string) => favorites.value.includes(id))

  // Actions
  function setInstitutions(list: InstitutionWithDistance[]) {
    institutions.value = list
  }

  function setCurrentInstitution(inst: InstitutionWithDistance | null) {
    currentInstitution.value = inst
    if (inst) {
      addToHistory(inst.id)
    }
  }

  function setFilters(newFilters: Partial<SearchFilters>) {
    filters.value = { ...filters.value, ...newFilters }
  }

  function resetFilters() {
    filters.value = { sortBy: 'rating' }
  }

  function setLoading(value: boolean) {
    loading.value = value
  }

  // 收藏管理
  function toggleFavorite(id: string) {
    const index = favorites.value.indexOf(id)
    if (index > -1) {
      favorites.value.splice(index, 1)
    } else {
      favorites.value.push(id)
    }
    saveFavoritesToStorage()
  }

  function addFavorite(id: string) {
    if (!favorites.value.includes(id)) {
      favorites.value.push(id)
      saveFavoritesToStorage()
    }
  }

  function removeFavorite(id: string) {
    const index = favorites.value.indexOf(id)
    if (index > -1) {
      favorites.value.splice(index, 1)
      saveFavoritesToStorage()
    }
  }

  function saveFavoritesToStorage() {
    try {
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites.value))
    } catch (e) {
      console.error('Failed to save favorites:', e)
    }
  }

  function loadFavoritesFromStorage() {
    try {
      const stored = localStorage.getItem(FAVORITES_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        if (Array.isArray(parsed)) {
          favorites.value = parsed
        }
      }
    } catch (e) {
      console.error('Failed to load favorites:', e)
      favorites.value = []
    }
  }

  // 浏览历史管理
  function addToHistory(id: string) {
    // 移除已存在的记录
    const existingIndex = browsingHistory.value.findIndex(h => h.id === id)
    if (existingIndex > -1) {
      browsingHistory.value.splice(existingIndex, 1)
    }
    
    // 添加到开头
    browsingHistory.value.unshift({
      id,
      visitedAt: new Date().toISOString()
    })
    
    // 限制历史记录数量
    if (browsingHistory.value.length > MAX_HISTORY) {
      browsingHistory.value = browsingHistory.value.slice(0, MAX_HISTORY)
    }
    
    saveHistoryToStorage()
  }

  function clearHistory() {
    browsingHistory.value = []
    saveHistoryToStorage()
  }

  function saveHistoryToStorage() {
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(browsingHistory.value))
    } catch (e) {
      console.error('Failed to save history:', e)
    }
  }

  function loadHistoryFromStorage() {
    try {
      const stored = localStorage.getItem(HISTORY_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        if (Array.isArray(parsed)) {
          browsingHistory.value = parsed
        }
      }
    } catch (e) {
      console.error('Failed to load history:', e)
      browsingHistory.value = []
    }
  }

  // 初始化
  function init() {
    loadFavoritesFromStorage()
    loadHistoryFromStorage()
  }

  return {
    // State
    institutions,
    currentInstitution,
    filters,
    loading,
    favorites,
    browsingHistory,
    // Getters
    favoriteCount,
    historyCount,
    isFavorite,
    // Actions
    setInstitutions,
    setCurrentInstitution,
    setFilters,
    resetFilters,
    setLoading,
    toggleFavorite,
    addFavorite,
    removeFavorite,
    addToHistory,
    clearHistory,
    init
  }
})
