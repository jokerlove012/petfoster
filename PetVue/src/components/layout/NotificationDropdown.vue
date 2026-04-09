<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore, useAuthStore } from '@/stores'
import type { Notification } from '@/types/notification'

const router = useRouter()
const notificationStore = useNotificationStore()
const authStore = useAuthStore()
const isOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)
let refreshTimer: ReturnType<typeof setInterval> | null = null

const notifications = computed(() => notificationStore.notifications.slice(0, 5))
const unreadCount = computed(() => notificationStore.unreadCount)
const isAuthenticated = computed(() => authStore.isAuthenticated)

// 获取通知数据
const fetchNotifications = async () => {
  if (isAuthenticated.value) {
    try {
      await notificationStore.fetchNotifications()
    } catch (e) {
      console.debug('Notification fetch failed, skipping')
    }
  }
}

// 启动自动刷新
const startAutoRefresh = () => {
  if (refreshTimer) return
  // 每30秒刷新一次通知（降低频率减少请求）
  refreshTimer = setInterval(() => {
    if (isAuthenticated.value && !document.hidden) {
      fetchNotifications()
    }
  }, 30000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 页面可见性变化处理
const handleVisibilityChange = () => {
  if (document.hidden) {
    stopAutoRefresh()
  } else {
    fetchNotifications()
    startAutoRefresh()
  }
}

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const closeDropdown = () => {
  isOpen.value = false
}

const handleNotificationClick = (notification: Notification) => {
  notificationStore.markAsRead(notification.id)
  closeDropdown()
  
  // 根据通知类型跳转
  if (notification.link) {
    router.push(notification.link)
  }
}

const markAllAsRead = () => {
  notificationStore.markAllAsRead()
}

const viewAll = () => {
  closeDropdown()
  router.push('/notifications')
}

// 格式化时间
const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 获取通知图标
const getNotificationIcon = (type: string) => {
  const icons: Record<string, string> = {
    booking: '📋',
    payment: '💰',
    health: '🏥',
    review: '⭐',
    system: '🔔',
    message: '💬'
  }
  return icons[type] || '🔔'
}

// 点击外部关闭
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (dropdownRef.value && !dropdownRef.value.contains(target)) {
    closeDropdown()
  }
}

// 监听登录状态变化
watch(isAuthenticated, (newVal) => {
  if (newVal) {
    fetchNotifications()
    startAutoRefresh()
  } else {
    stopAutoRefresh()
    notificationStore.clearAll()
  }
})

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 初始加载通知
  if (isAuthenticated.value) {
    fetchNotifications()
    startAutoRefresh()
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  stopAutoRefresh()
})
</script>

<template>
  <div ref="dropdownRef" class="notification-dropdown">
    <button class="trigger-btn" @click="toggleDropdown" :class="{ active: isOpen }">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
      </svg>
      <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="dropdown-panel">
        <div class="panel-header">
          <h3>通知</h3>
          <button v-if="unreadCount > 0" class="mark-read-btn" @click="markAllAsRead">
            全部已读
          </button>
        </div>

        <div class="notification-list">
          <div v-if="notifications.length === 0" class="empty-state">
            <span class="empty-icon">🔔</span>
            <p>暂无通知</p>
          </div>

          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.isRead }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-icon">
              {{ getNotificationIcon(notification.type) }}
            </div>
            <div class="notification-content">
              <p class="notification-title">{{ notification.title }}</p>
              <p class="notification-body">{{ notification.content }}</p>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
            <div v-if="!notification.isRead" class="unread-dot"></div>
          </div>
        </div>

        <div class="panel-footer">
          <button class="view-all-btn" @click="viewAll">
            查看全部通知
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>


<style lang="scss" scoped>
.notification-dropdown {
  position: relative;
}

.trigger-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  transition: all 150ms ease;

  &:hover,
  &.active {
    background: var(--color-neutral-100);
    color: var(--color-primary);
  }

  .badge {
    position: absolute;
    top: 4px;
    right: 4px;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    background: var(--color-error);
    color: white;
    font-size: 11px;
    font-weight: 600;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.dropdown-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 360px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--color-border);
  overflow: hidden;
  z-index: 1000;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);

  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
  }

  .mark-read-btn {
    border: none;
    background: none;
    color: var(--color-primary);
    font-size: 13px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: var(--radius-sm);

    &:hover {
      background: var(--color-primary-light);
    }
  }
}

.notification-list {
  max-height: 360px;
  overflow-y: auto;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;

  .empty-icon {
    font-size: 40px;
    display: block;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    color: var(--color-text-muted);
    margin: 0;
  }
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 150ms ease;
  position: relative;

  &:hover {
    background: var(--color-neutral-50);
  }

  &.unread {
    background: var(--color-primary-light);

    &:hover {
      background: rgba(255, 107, 53, 0.15);
    }
  }

  &:not(:last-child) {
    border-bottom: 1px solid var(--color-border);
  }
}

.notification-icon {
  width: 36px;
  height: 36px;
  background: var(--color-neutral-100);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;

  .notification-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text-primary);
    margin: 0 0 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .notification-body {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin: 0 0 6px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .notification-time {
    font-size: 12px;
    color: var(--color-text-muted);
  }
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
}

.panel-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);

  .view-all-btn {
    width: 100%;
    padding: 10px;
    border: none;
    background: var(--color-neutral-100);
    color: var(--color-text-primary);
    font-size: 14px;
    font-weight: 500;
    border-radius: var(--radius-md);
    cursor: pointer;
    transition: all 150ms ease;

    &:hover {
      background: var(--color-primary);
      color: white;
    }
  }
}

// 动画
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 200ms ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 480px) {
  .dropdown-panel {
    width: calc(100vw - 32px);
    right: -8px;
  }
}
</style>
