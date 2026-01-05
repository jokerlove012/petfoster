<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useNotificationStore, type Notification } from '@/stores/notification'
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import NotificationCard from '@/components/notification/NotificationCard.vue'
import { Bell, Check, Filter, Trash2 } from 'lucide-vue-next'

const notificationStore = useNotificationStore()

const loading = ref(false)
const selectedFilter = ref<string>('all')

// 筛选选项 - 包含后端发送的类型
const filterOptions = [
  { value: 'all', label: '全部' },
  { value: 'booking', label: '预约通知' },
  { value: 'payment', label: '支付通知' },
  { value: 'health', label: '健康通知' },
  { value: 'review', label: '评价通知' },
  { value: 'message', label: '消息' },
  { value: 'system', label: '系统通知' }
]

// 筛选后的通知
const filteredNotifications = computed(() => {
  if (selectedFilter.value === 'all') {
    return notificationStore.notifications
  }
  return notificationStore.notifications.filter(n => n.type === selectedFilter.value)
})

// 未读通知数
const unreadCount = computed(() => notificationStore.unreadCount)

// 加载通知
const loadNotifications = async () => {
  await notificationStore.fetchNotifications()
}

// 首次加载
const initialLoad = async () => {
  loading.value = true
  try {
    await loadNotifications()
  } finally {
    loading.value = false
  }
}

// 自动刷新
useAutoRefresh(loadNotifications, 5000)

// 标记为已读
const markAsRead = async (notification: Notification) => {
  if (!notification.isRead) {
    await notificationStore.markAsRead(notification.id)
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  await notificationStore.markAllAsRead()
}

// 删除通知
const deleteNotification = async (id: string) => {
  await notificationStore.deleteNotification(id)
}

// 清空已读通知
const clearReadNotifications = async () => {
  await notificationStore.clearReadNotifications()
}

onMounted(() => {
  initialLoad()
})
</script>

<template>
  <div class="notification-center">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>
          <Bell :size="24" />
          通知中心
        </h1>
        <span v-if="unreadCount > 0" class="unread-badge">
          {{ unreadCount }} 条未读
        </span>
      </div>
      <div class="header-actions">
        <button 
          v-if="unreadCount > 0"
          class="action-btn"
          @click="markAllAsRead"
        >
          <Check :size="18" />
          全部已读
        </button>
        <button 
          class="action-btn"
          @click="clearReadNotifications"
        >
          <Trash2 :size="18" />
          清除已读
        </button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <Filter :size="18" />
      <div class="filter-options">
        <button
          v-for="option in filterOptions"
          :key="option.value"
          class="filter-btn"
          :class="{ active: selectedFilter === option.value }"
          @click="selectedFilter = option.value"
        >
          {{ option.label }}
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredNotifications.length === 0" class="empty-state">
      <div class="empty-icon">🔔</div>
      <h3>暂无通知</h3>
      <p>{{ selectedFilter === 'all' ? '您还没有收到任何通知' : '没有此类型的通知' }}</p>
    </div>

    <!-- 通知列表 -->
    <div v-else class="notification-list">
      <NotificationCard
        v-for="notification in filteredNotifications"
        :key="notification.id"
        :notification="notification"
        @click="markAsRead(notification)"
        @delete="deleteNotification(notification.id)"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.notification-center {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  h1 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
  
  .unread-badge {
    padding: 4px 12px;
    background: var(--color-primary);
    color: white;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-background);
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
  overflow-x: auto;
  
  .filter-options {
    display: flex;
    gap: 8px;
  }
}

.filter-btn {
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--color-primary);
  }
  
  &.active {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: white;
  }
}

.loading-state {
  text-align: center;
  padding: 48px 24px;
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--color-border);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 64px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
