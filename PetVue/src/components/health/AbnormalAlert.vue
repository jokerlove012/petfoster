<script setup lang="ts">
import { computed } from 'vue'
import type { HealthRecord } from '@/types/health'
import { AppButton } from '@/components/common'

const props = defineProps<{
  record: HealthRecord
}>()

const emit = defineEmits<{
  (e: 'contact'): void
  (e: 'dismiss'): void
}>()

// 格式化时间
const formatTime = (date: Date | string) => {
  const d = typeof date === 'string' ? new Date(date) : date
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  
  if (diffHours < 1) {
    return '刚刚'
  }
  if (diffHours < 24) {
    return `${diffHours}小时前`
  }
  
  return d.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 联系机构
const handleContact = () => {
  emit('contact')
}

// 关闭提醒
const handleDismiss = () => {
  emit('dismiss')
}
</script>

<template>
  <div class="abnormal-alert">
    <div class="alert-header">
      <div class="alert-icon">⚠️</div>
      <div class="alert-title">
        <h4>健康异常提醒</h4>
        <span class="alert-time">{{ formatTime(record.createdAt) }}</span>
      </div>
      <button class="dismiss-btn" @click="handleDismiss">×</button>
    </div>
    
    <div class="alert-content">
      <p class="alert-message">{{ record.abnormalDetails || record.healthObservations }}</p>
      
      <div class="alert-details">
        <div class="detail-item" v-if="record.feedingStatus !== 'normal'">
          <span class="detail-label">进食状态：</span>
          <span class="detail-value warning">
            {{ record.feedingStatus === 'reduced' ? '减少' : 
               record.feedingStatus === 'refused' ? '拒食' : '异常' }}
          </span>
        </div>
        <div class="detail-item" v-if="record.activityLevel === 'low' || record.activityLevel === 'inactive'">
          <span class="detail-label">活动水平：</span>
          <span class="detail-value warning">
            {{ record.activityLevel === 'low' ? '较低' : '不活跃' }}
          </span>
        </div>
        <div class="detail-item" v-if="record.mood === 'anxious' || record.mood === 'stressed'">
          <span class="detail-label">情绪状态：</span>
          <span class="detail-value warning">
            {{ record.mood === 'anxious' ? '焦虑' : '紧张' }}
          </span>
        </div>
      </div>
    </div>
    
    <div class="alert-actions">
      <AppButton type="outline" size="sm" @click="handleDismiss">
        我知道了
      </AppButton>
      <AppButton type="primary" size="sm" @click="handleContact">
        📞 联系机构
      </AppButton>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.abnormal-alert {
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  border: 1px solid #F59E0B;
  border-radius: var(--radius-xl);
  padding: 16px;
  margin-bottom: 16px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.alert-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.alert-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.alert-title {
  flex: 1;
  
  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #92400E;
    margin: 0 0 2px;
  }
  
  .alert-time {
    font-size: 12px;
    color: #B45309;
  }
}

.dismiss-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(146, 64, 14, 0.1);
  border-radius: 50%;
  font-size: 16px;
  color: #92400E;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background: rgba(146, 64, 14, 0.2);
  }
}

.alert-content {
  margin-bottom: 12px;
}

.alert-message {
  font-size: 14px;
  line-height: 1.6;
  color: #78350F;
  margin: 0 0 12px;
}

.alert-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  
  .detail-label {
    color: #92400E;
  }
  
  .detail-value {
    font-weight: 600;
    
    &.warning {
      color: #DC2626;
    }
  }
}

.alert-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  
  :deep(.app-button--outline) {
    border-color: #92400E;
    color: #92400E;
    
    &:hover {
      background: rgba(146, 64, 14, 0.1);
    }
  }
}
</style>
