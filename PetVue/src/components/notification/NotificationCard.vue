<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Notification, NotificationType } from '@/types/notification'
import { 
  Calendar, 
  CheckCircle, 
  XCircle, 
  Heart, 
  AlertTriangle, 
  Star, 
  MessageCircle, 
  Info,
  Trash2,
  ChevronRight
} from 'lucide-vue-next'

const props = defineProps<{
  notification: Notification
}>()

const emit = defineEmits<{
  click: []
  delete: []
}>()

const router = useRouter()

// 通知类型配置 - 支持后端发送的类型和前端定义的类型
const typeConfig: Record<string, { icon: any; color: string; label: string }> = {
  // 后端发送的类型
  booking: { icon: Calendar, color: '#1890ff', label: '预约通知' },
  payment: { icon: CheckCircle, color: '#52c41a', label: '支付通知' },
  health: { icon: Heart, color: '#eb2f96', label: '健康通知' },
  review: { icon: Star, color: '#722ed1', label: '评价通知' },
  message: { icon: MessageCircle, color: '#13c2c2', label: '消息' },
  system: { icon: Info, color: '#8c8c8c', label: '系统通知' },
  // 前端定义的类型（兼容）
  booking_created: { icon: Calendar, color: '#1890ff', label: '预约创建' },
  booking_confirmed: { icon: CheckCircle, color: '#52c41a', label: '预约确认' },
  booking_cancelled: { icon: XCircle, color: '#ff4d4f', label: '预约取消' },
  health_update: { icon: Heart, color: '#eb2f96', label: '健康更新' },
  health_alert: { icon: AlertTriangle, color: '#faad14', label: '健康提醒' },
  review_reminder: { icon: Star, color: '#722ed1', label: '评价提醒' }
}

// 默认配置，防止未知类型报错
const defaultConfig = { icon: Info, color: '#8c8c8c', label: '通知' }

const config = computed(() => typeConfig[props.notification.type] || defaultConfig)

// 格式化时间
const formattedTime = computed(() => {
  const date = props.notification.createdAt instanceof Date 
    ? props.notification.createdAt 
    : new Date(props.notification.createdAt)
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 7) return `${days} 天前`
  
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
})

// 点击通知
const handleClick = () => {
  emit('click')
  
  // 如果有link字段，直接跳转
  if ((props.notification as any).link) {
    router.push((props.notification as any).link)
    return
  }
  
  // 根据类型跳转（兼容旧逻辑）
  if (props.notification.relatedId) {
    switch (props.notification.type) {
      case 'booking':
      case 'booking_created':
      case 'booking_confirmed':
      case 'booking_cancelled':
        router.push(`/booking/order/${props.notification.relatedId}`)
        break
      case 'health':
      case 'health_update':
      case 'health_alert':
        router.push(`/booking/order/${props.notification.relatedId}`)
        break
      case 'review':
      case 'review_reminder':
        router.push(`/review/create/${props.notification.relatedId}`)
        break
    }
  }
}

// 删除通知
const handleDelete = (e: Event) => {
  e.stopPropagation()
  emit('delete')
}
</script>

<template>
  <div 
    class="notification-card"
    :class="{ unread: !notification.isRead }"
    @click="handleClick"
  >
    <!-- 图标 -->
    <div class="notification-icon" :style="{ backgroundColor: config.color + '15', color: config.color }">
      <component :is="config.icon" :size="20" />
    </div>

    <!-- 内容 -->
    <div class="notification-content">
      <div class="notification-header">
        <span class="notification-type" :style="{ color: config.color }">
          {{ config.label }}
        </span>
        <span class="notification-time">{{ formattedTime }}</span>
      </div>
      <h4 class="notification-title">{{ notification.title }}</h4>
      <p class="notification-text">{{ notification.content }}</p>
    </div>

    <!-- 操作 -->
    <div class="notification-actions">
      <button class="delete-btn" @click="handleDelete" title="删除">
        <Trash2 :size="16" />
      </button>
      <ChevronRight :size="20" class="arrow-icon" />
    </div>

    <!-- 未读标记 -->
    <div v-if="!notification.isRead" class="unread-dot"></div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.notification-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  
  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-sm);
    
    .delete-btn {
      opacity: 1;
    }
  }
  
  &.unread {
    background: var(--color-primary-light);
    border-color: var(--color-primary);
  }
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.notification-type {
  font-size: 12px;
  font-weight: 500;
}

.notification-time {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--color-text-primary);
}

.notification-text {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.delete-btn {
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-error);
    color: white;
  }
}

.arrow-icon {
  color: var(--color-text-tertiary);
}

.unread-dot {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
}
</style>
