import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi } from '@/api/notification'
import type { Notification as ApiNotification } from '@/api/notification'

export type NotificationType = 'booking' | 'payment' | 'health' | 'review' | 'system' | 'message'

export interface Notification {
  id: string
  type: NotificationType
  title: string
  content: string
  isRead: boolean
  createdAt: string
  link?: string
  data?: Record<string, unknown>
}

export interface NotificationPreferences {
  booking: boolean
  payment: boolean
  health: boolean
  review: boolean
  system: boolean
  message: boolean
  email: boolean
  push: boolean
  sms: boolean
}

const PREFERENCES_KEY = 'pet_foster_notification_prefs'

export const useNotificationStore = defineStore('notification', () => {
  // State
  const notifications = ref<Notification[]>([])
  const loading = ref(false)
  const preferences = ref<NotificationPreferences>({
    booking: true,
    payment: true,
    health: true,
    review: true,
    system: true,
    message: true,
    email: true,
    push: true,
    sms: false
  })

  // Getters
  const unreadCount = computed(() => 
    notifications.value.filter(n => !n.isRead).length
  )

  const unreadNotifications = computed(() => 
    notifications.value.filter(n => !n.isRead)
  )

  const readNotifications = computed(() => 
    notifications.value.filter(n => n.isRead)
  )

  const notificationsByType = computed(() => (type: NotificationType) => 
    notifications.value.filter(n => n.type === type)
  )

  const sortedNotifications = computed(() => 
    [...notifications.value].sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  )

  // Actions
  function setNotifications(list: Notification[]) {
    notifications.value = list
  }

  function addNotification(notification: Notification) {
    notifications.value.unshift(notification)
  }

  async function markAsRead(id: string) {
    try {
      const res = await notificationApi.markAsRead(id)
      if (res.code === 200) {
        const notification = notifications.value.find(n => n.id === id)
        if (notification) {
          notification.isRead = true
        }
      }
    } catch (e) {
      console.error('Failed to mark as read:', e)
    }
  }

  async function markAllAsRead() {
    try {
      const res = await notificationApi.markAllAsRead()
      if (res.code === 200) {
        notifications.value.forEach(n => {
          n.isRead = true
        })
      }
    } catch (e) {
      console.error('Failed to mark all as read:', e)
    }
  }

  function removeNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  function clearAll() {
    notifications.value = []
  }

  async function clearReadNotifications() {
    try {
      const res = await notificationApi.clearRead()
      if (res.code === 200) {
        notifications.value = notifications.value.filter(n => !n.isRead)
      }
    } catch (e) {
      console.error('Failed to clear read:', e)
    }
  }

  async function deleteNotification(id: string) {
    try {
      const res = await notificationApi.delete(id)
      if (res.code === 200) {
        removeNotification(id)
      }
    } catch (e) {
      console.error('Failed to delete notification:', e)
    }
  }

  function setLoading(value: boolean) {
    loading.value = value
  }

  // 获取通知列表
  async function fetchNotifications() {
    setLoading(true)
    try {
      const res = await notificationApi.list()
      if (res.code === 200 && res.data) {
        notifications.value = res.data.map((n: ApiNotification) => ({
          id: n.id,
          type: n.type as NotificationType,
          title: n.title,
          content: n.content,
          isRead: n.isRead,
          createdAt: n.createdAt,
          link: n.link
        }))
      }
    } catch (e) {
      console.error('Failed to fetch notifications:', e)
    } finally {
      setLoading(false)
    }
  }

  // 获取未读数量
  async function fetchUnreadCount() {
    try {
      const res = await notificationApi.getUnreadCount()
      if (res.code === 200 && res.data) {
        return res.data.count
      }
    } catch (e) {
      console.error('Failed to fetch unread count:', e)
    }
    return 0
  }

  // 获取偏好设置
  async function getPreferences() {
    loadPreferencesFromStorage()
    return preferences.value
  }

  // 偏好设置
  function updatePreferences(updates: Partial<NotificationPreferences>) {
    preferences.value = { ...preferences.value, ...updates }
    savePreferencesToStorage()
  }

  function savePreferencesToStorage() {
    try {
      localStorage.setItem(PREFERENCES_KEY, JSON.stringify(preferences.value))
    } catch (e) {
      console.error('Failed to save notification preferences:', e)
    }
  }

  function loadPreferencesFromStorage() {
    try {
      const stored = localStorage.getItem(PREFERENCES_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        preferences.value = { ...preferences.value, ...parsed }
      }
    } catch (e) {
      console.error('Failed to load notification preferences:', e)
    }
  }

  function init() {
    loadPreferencesFromStorage()
  }

  return {
    // State
    notifications,
    loading,
    preferences,
    // Getters
    unreadCount,
    unreadNotifications,
    readNotifications,
    notificationsByType,
    sortedNotifications,
    // Actions
    setNotifications,
    addNotification,
    markAsRead,
    markAllAsRead,
    removeNotification,
    deleteNotification,
    clearAll,
    clearReadNotifications,
    setLoading,
    updatePreferences,
    fetchNotifications,
    fetchUnreadCount,
    getPreferences,
    init
  }
})
