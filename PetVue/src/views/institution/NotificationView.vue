<script setup lang="ts">
import { ref, computed } from 'vue'
import { Bell, Check, CheckCheck, Trash2, Filter, Package, Star, AlertCircle, Info } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'

const typeFilter = ref('all')

// 通知统计
const notificationStats = ref({
  unread: 8,
  total: 56
})

// 通知列表
const notifications = ref([
  { id: '1', type: 'order', title: '新订单提醒', content: '您有一个新的寄养订单 ORD20250115001，请及时处理', time: '2025-01-15 10:30', read: false },
  { id: '2', type: 'order', title: '订单即将到期', content: '订单 ORD20250114002 的寄养服务将于明天结束，请做好交接准备', time: '2025-01-15 09:00', read: false },
  { id: '3', type: 'review', title: '新评价提醒', content: '用户张三对您的服务进行了评价，点击查看详情', time: '2025-01-14 18:20', read: false },
  { id: '4', type: 'system', title: '系统维护通知', content: '平台将于1月20日凌晨2:00-4:00进行系统维护，届时部分功能可能无法使用', time: '2025-01-14 15:00', read: true },
  { id: '5', type: 'alert', title: '投诉处理提醒', content: '您有一条待处理的投诉 CP001，请尽快回复', time: '2025-01-14 11:30', read: false },
  { id: '6', type: 'order', title: '订单取消通知', content: '订单 ORD20250113005 已被用户取消', time: '2025-01-13 16:45', read: true },
  { id: '7', type: 'system', title: '结算完成通知', content: '您的1月上半月结算已完成，金额 ¥12,580 已转入您的账户', time: '2025-01-13 10:00', read: true }
])

const filteredNotifications = computed(() => {
  if (typeFilter.value === 'all') return notifications.value
  if (typeFilter.value === 'unread') return notifications.value.filter(n => !n.read)
  return notifications.value.filter(n => n.type === typeFilter.value)
})

const getTypeIcon = (type: string) => {
  const map: Record<string, any> = { order: Package, review: Star, alert: AlertTriangle, system: Info }
  return map[type] || Bell
}

const getTypeColor = (type: string) => {
  const map: Record<string, string> = { order: '#722ed1', review: '#faad14', alert: '#ff4d4f', system: '#1890ff' }
  return map[type] || '#6B6560'
}

const markAsRead = (notification: typeof notifications.value[0]) => {
  notification.read = true
  notificationStats.value.unread--
}

const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
  notificationStats.value.unread = 0
  ElMessage.success('已全部标记为已读')
}

const deleteNotification = (id: string) => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index > -1) {
    if (!notifications.value[index].read) notificationStats.value.unread--
    notifications.value.splice(index, 1)
    notificationStats.value.total--
  }
}
</script>

<template>
  <div class="notification-view">
    <div class="page-header">
      <div class="header-left">
        <h1>🔔 通知中心</h1>
        <p>查看系统通知和消息提醒</p>
      </div>
      <div class="header-actions">
        <span class="unread-badge">{{ notificationStats.unread }} 条未读</span>
        <button class="btn-mark-all" @click="markAllAsRead"><CheckCheck :size="16" /> 全部已读</button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <button v-for="t in ['all', 'unread', 'order', 'review', 'alert', 'system']" :key="t"
        :class="{ active: typeFilter === t }" @click="typeFilter = t">
        {{ { all: '全部', unread: '未读', order: '订单', review: '评价', alert: '警告', system: '系统' }[t] }}
      </button>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <div v-for="item in filteredNotifications" :key="item.id" 
        class="notification-item" :class="{ unread: !item.read }" @click="markAsRead(item)">
        <div class="notification-icon" :style="{ background: getTypeColor(item.type) + '15', color: getTypeColor(item.type) }">
          <component :is="getTypeIcon(item.type)" :size="20" />
        </div>
        <div class="notification-content">
          <div class="notification-header">
            <span class="title">{{ item.title }}</span>
            <span class="time">{{ item.time }}</span>
          </div>
          <p class="content">{{ item.content }}</p>
        </div>
        <button class="btn-delete" @click.stop="deleteNotification(item.id)"><Trash2 :size="16" /></button>
      </div>
      <div v-if="filteredNotifications.length === 0" class="empty-state">
        <Bell :size="48" />
        <p>暂无通知</p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.notification-view { max-width: 900px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
  .header-left { h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; } p { color: #6B6560; margin: 0; } }
  .header-actions { display: flex; align-items: center; gap: 16px;
    .unread-badge { background: #ff4d4f; color: white; padding: 4px 12px; border-radius: 20px; font-size: 13px; }
    .btn-mark-all { display: flex; align-items: center; gap: 6px; padding: 8px 16px; background: #F8F8F7; border: none; border-radius: 8px; font-size: 13px; cursor: pointer; }
  }
}

.filter-bar {
  display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap;
  button { padding: 8px 16px; border: none; background: white; border-radius: 8px; font-size: 13px; color: #6B6560; cursor: pointer;
    &.active { background: #722ed1; color: white; }
  }
}

.notification-list { display: flex; flex-direction: column; gap: 12px; }

.notification-item {
  display: flex; align-items: flex-start; gap: 16px; padding: 16px 20px;
  background: white; border-radius: 12px; cursor: pointer; transition: all 0.2s;
  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
  &.unread { border-left: 3px solid #722ed1; background: #FDFCFF; }
  .notification-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
  .notification-content { flex: 1;
    .notification-header { display: flex; justify-content: space-between; margin-bottom: 6px;
      .title { font-weight: 600; color: #2D2A26; }
      .time { font-size: 12px; color: #9A958F; }
    }
    .content { font-size: 14px; color: #6B6560; margin: 0; line-height: 1.5; }
  }
  .btn-delete { background: none; border: none; color: #9A958F; cursor: pointer; padding: 4px; &:hover { color: #ff4d4f; } }
}

.empty-state { text-align: center; padding: 60px 20px; color: #9A958F;
  p { margin-top: 16px; }
}
</style>
