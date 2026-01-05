<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import { Bell, Mail, Smartphone, Monitor, Save } from 'lucide-vue-next'

const notificationStore = useNotificationStore()

const loading = ref(false)
const saving = ref(false)

// 通知偏好设置 - 匹配 store 的扁平结构
const preferences = ref({
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

// 通知类型配置
const notificationTypes = [
  { key: 'booking', label: '预约通知', description: '预约创建、确认、取消等' },
  { key: 'payment', label: '支付通知', description: '支付成功、退款等' },
  { key: 'health', label: '健康通知', description: '宠物健康更新和异常提醒' },
  { key: 'message', label: '消息通知', description: '与机构的聊天消息' },
  { key: 'review', label: '评价通知', description: '评价回复和提醒' },
  { key: 'system', label: '系统通知', description: '系统公告和活动通知' }
] as const

// 渠道配置
const channels = [
  { key: 'email', label: '邮件通知', icon: Mail, description: '通过邮件接收通知' },
  { key: 'push', label: '推送通知', icon: Smartphone, description: '通过手机推送接收通知' },
  { key: 'sms', label: '短信通知', icon: Monitor, description: '通过短信接收通知' }
] as const

// 加载设置
const loadPreferences = async () => {
  loading.value = true
  try {
    const prefs = await notificationStore.getPreferences()
    if (prefs) {
      preferences.value = { ...preferences.value, ...prefs }
    }
  } finally {
    loading.value = false
  }
}

// 保存设置
const savePreferences = async () => {
  saving.value = true
  try {
    notificationStore.updatePreferences(preferences.value)
  } finally {
    saving.value = false
  }
}

// 切换单个设置
const toggleSetting = (key: string) => {
  const k = key as keyof typeof preferences.value
  preferences.value[k] = !preferences.value[k]
}

onMounted(() => {
  loadPreferences()
})
</script>

<template>
  <div class="notification-settings">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>
        <Bell :size="24" />
        通知设置
      </h1>
      <p>管理您接收通知的方式和类型</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 设置内容 -->
    <div v-else class="settings-content">
      <!-- 通知类型 -->
      <div class="section">
        <h2>通知类型</h2>
        <div class="options-list">
          <div 
            v-for="type in notificationTypes"
            :key="type.key"
            class="option-item"
          >
            <div class="option-info">
              <span class="option-label">{{ type.label }}</span>
              <span class="option-desc">{{ type.description }}</span>
            </div>
            <label class="toggle">
              <input 
                type="checkbox"
                :checked="preferences[type.key]"
                @change="toggleSetting(type.key)"
              />
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <!-- 通知渠道 -->
      <div class="section">
        <h2>通知渠道</h2>
        <div class="options-list">
          <div 
            v-for="channel in channels"
            :key="channel.key"
            class="option-item"
          >
            <div class="option-info">
              <component :is="channel.icon" :size="18" class="channel-icon" />
              <div>
                <span class="option-label">{{ channel.label }}</span>
                <span class="option-desc">{{ channel.description }}</span>
              </div>
            </div>
            <label class="toggle">
              <input 
                type="checkbox"
                :checked="preferences[channel.key]"
                @change="toggleSetting(channel.key)"
              />
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <!-- 保存按钮 -->
      <div class="save-section">
        <button 
          class="save-btn"
          :disabled="saving"
          @click="savePreferences"
        >
          <Save :size="18" />
          {{ saving ? '保存中...' : '保存设置' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.notification-settings {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
  
  h1 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
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

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section {
  background: var(--color-surface);
  border-radius: 12px;
  border: 1px solid var(--color-border);
  padding: 20px;
  
  h2 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  
  &:hover {
    background: var(--color-background);
  }
}

.option-info {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .channel-icon {
    color: var(--color-text-secondary);
  }
  
  > div {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }
  
  .option-label {
    font-size: 14px;
    font-weight: 500;
  }
  
  .option-desc {
    font-size: 12px;
    color: var(--color-text-secondary);
  }
}

// Toggle 开关样式
.toggle {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  
  input {
    opacity: 0;
    width: 0;
    height: 0;
    
    &:checked + .toggle-slider {
      background: var(--color-primary);
      
      &::before {
        transform: translateX(20px);
      }
    }
  }
  
  .toggle-slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--color-border);
    border-radius: 24px;
    transition: all 0.3s;
    
    &::before {
      content: '';
      position: absolute;
      height: 18px;
      width: 18px;
      left: 3px;
      bottom: 3px;
      background: white;
      border-radius: 50%;
      transition: all 0.3s;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }
}

.save-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.save-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: var(--color-primary);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover:not(:disabled) {
    opacity: 0.9;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
