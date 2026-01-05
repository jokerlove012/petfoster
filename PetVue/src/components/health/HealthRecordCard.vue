<script setup lang="ts">
import { computed } from 'vue'
import type { HealthRecord, FeedingStatus, ActivityLevel, PetMood } from '@/types/health'

const props = defineProps<{
  record: HealthRecord
}>()

// 喂食状态配置
const feedingConfig: Record<FeedingStatus, { label: string; icon: string; color: string }> = {
  normal: { label: '正常', icon: '🍽️', color: 'var(--color-success)' },
  reduced: { label: '减少', icon: '📉', color: 'var(--color-warning)' },
  increased: { label: '增加', icon: '📈', color: 'var(--color-info)' },
  refused: { label: '拒食', icon: '🚫', color: 'var(--color-error)' }
}

// 活动水平配置
const activityConfig: Record<ActivityLevel, { label: string; icon: string; color: string }> = {
  high: { label: '活跃', icon: '🏃', color: 'var(--color-success)' },
  normal: { label: '正常', icon: '🚶', color: 'var(--color-info)' },
  low: { label: '较低', icon: '😴', color: 'var(--color-warning)' },
  inactive: { label: '不活跃', icon: '💤', color: 'var(--color-error)' }
}

// 心情配置
const moodConfig: Record<PetMood, { label: string; icon: string; color: string }> = {
  happy: { label: '开心', icon: '😊', color: 'var(--color-success)' },
  calm: { label: '平静', icon: '😌', color: 'var(--color-info)' },
  anxious: { label: '焦虑', icon: '😟', color: 'var(--color-warning)' },
  stressed: { label: '紧张', icon: '😰', color: 'var(--color-error)' }
}

// 格式化时间
const formatTime = (date: Date | string) => {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <div class="health-record-card" :class="{ abnormal: record.isAbnormal }">
    <!-- 卡片头部 -->
    <div class="card-header">
      <span class="record-time">{{ formatTime(record.createdAt) }}</span>
      <span v-if="record.isAbnormal" class="abnormal-badge">⚠️ 异常</span>
    </div>

    <!-- 状态指标 -->
    <div class="status-indicators">
      <div class="indicator">
        <span class="indicator-icon">{{ feedingConfig[record.feedingStatus].icon }}</span>
        <div class="indicator-info">
          <span class="indicator-label">进食</span>
          <span 
            class="indicator-value"
            :style="{ color: feedingConfig[record.feedingStatus].color }"
          >
            {{ feedingConfig[record.feedingStatus].label }}
          </span>
        </div>
      </div>
      
      <div class="indicator">
        <span class="indicator-icon">{{ activityConfig[record.activityLevel].icon }}</span>
        <div class="indicator-info">
          <span class="indicator-label">活动</span>
          <span 
            class="indicator-value"
            :style="{ color: activityConfig[record.activityLevel].color }"
          >
            {{ activityConfig[record.activityLevel].label }}
          </span>
        </div>
      </div>
      
      <div class="indicator">
        <span class="indicator-icon">{{ moodConfig[record.mood].icon }}</span>
        <div class="indicator-info">
          <span class="indicator-label">心情</span>
          <span 
            class="indicator-value"
            :style="{ color: moodConfig[record.mood].color }"
          >
            {{ moodConfig[record.mood].label }}
          </span>
        </div>
      </div>
    </div>

    <!-- 健康数据 -->
    <div v-if="record.weight || record.temperature" class="health-data">
      <div v-if="record.weight" class="data-item">
        <span class="data-icon">⚖️</span>
        <span class="data-value">{{ record.weight }} kg</span>
      </div>
      <div v-if="record.temperature" class="data-item">
        <span class="data-icon">🌡️</span>
        <span class="data-value">{{ record.temperature }}°C</span>
      </div>
    </div>

    <!-- 观察记录 -->
    <div class="observations">
      <p>{{ record.healthObservations }}</p>
    </div>

    <!-- 异常详情 -->
    <div v-if="record.isAbnormal && record.abnormalDetails" class="abnormal-details">
      <span class="abnormal-icon">⚠️</span>
      <span class="abnormal-text">{{ record.abnormalDetails }}</span>
    </div>

    <!-- 用药记录 -->
    <div v-if="record.medications && record.medications.length > 0" class="medications">
      <span class="medications-label">💊 用药：</span>
      <span class="medications-list">{{ record.medications.join('、') }}</span>
    </div>

    <!-- 照片/视频 -->
    <div v-if="(record.photos && record.photos.length > 0) || (record.videos && record.videos.length > 0)" class="media-section">
      <div class="media-grid">
        <div 
          v-for="(photo, index) in record.photos?.slice(0, 4)" 
          :key="'photo-' + index"
          class="media-item"
        >
          <div class="media-placeholder">📷</div>
        </div>
        <div 
          v-for="(video, index) in record.videos?.slice(0, 2)" 
          :key="'video-' + index"
          class="media-item video"
        >
          <div class="media-placeholder">🎬</div>
        </div>
      </div>
      <span class="media-count" v-if="(record.photos?.length || 0) + (record.videos?.length || 0) > 6">
        +{{ (record.photos?.length || 0) + (record.videos?.length || 0) - 6 }}
      </span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.health-record-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 16px;
  border: 1px solid var(--color-border);
  
  &.abnormal {
    border-color: var(--color-warning);
    background: rgba(245, 158, 11, 0.05);
  }
}

// 卡片头部
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  
  .record-time {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
  
  .abnormal-badge {
    font-size: 12px;
    padding: 2px 8px;
    background: rgba(245, 158, 11, 0.1);
    color: var(--color-warning);
    border-radius: var(--radius-full);
  }
}

// 状态指标
.status-indicators {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .indicator-icon {
    font-size: 20px;
  }
  
  .indicator-info {
    display: flex;
    flex-direction: column;
    
    .indicator-label {
      font-size: 11px;
      color: var(--color-text-tertiary);
    }
    
    .indicator-value {
      font-size: 13px;
      font-weight: 600;
    }
  }
}

// 健康数据
.health-data {
  display: flex;
  gap: 16px;
  padding: 8px 12px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  margin-bottom: 12px;
}

.data-item {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .data-icon {
    font-size: 14px;
  }
  
  .data-value {
    font-size: 14px;
    font-weight: 500;
  }
}

// 观察记录
.observations {
  margin-bottom: 12px;
  
  p {
    font-size: 14px;
    line-height: 1.6;
    color: var(--color-text-primary);
    margin: 0;
  }
}

// 异常详情
.abnormal-details {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: var(--radius-md);
  margin-bottom: 12px;
  
  .abnormal-icon {
    font-size: 14px;
  }
  
  .abnormal-text {
    font-size: 13px;
    color: var(--color-warning);
    font-weight: 500;
  }
}

// 用药记录
.medications {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
  
  .medications-label {
    font-weight: 500;
  }
}

// 媒体区域
.media-section {
  position: relative;
}

.media-grid {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.media-item {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  
  &.video {
    position: relative;
    
    &::after {
      content: '▶';
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.3);
      color: white;
      font-size: 16px;
    }
  }
}

.media-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent-light));
}

.media-count {
  position: absolute;
  right: 0;
  bottom: 0;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 12px;
  border-radius: var(--radius-sm);
}
</style>
