<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { HealthRecord } from '@/types/health'
import HealthRecordCard from './HealthRecordCard.vue'
import AbnormalAlert from './AbnormalAlert.vue'
import { healthApi } from '@/api/health'

const props = defineProps<{
  bookingId: string
}>()

const records = ref<HealthRecord[]>([])
const loading = ref(false)
const selectedDate = ref<string | null>(null)

// 加载健康记录
const loadRecords = async () => {
  loading.value = true
  try {
    const res = await healthApi.getByBookingId(props.bookingId)
    if (res.code === 200 && res.data) {
      records.value = res.data.map((item: any) => ({
        ...item,
        date: new Date(item.date),
        createdAt: item.createdAt ? new Date(item.createdAt) : new Date()
      }))
    }
  } catch (error) {
    console.error('Failed to load records:', error)
  } finally {
    loading.value = false
  }
}

// 按日期分组的记录
const groupedRecords = computed(() => {
  const groups: Record<string, HealthRecord[]> = {}
  
  // 按日期降序排序
  const sorted = [...records.value].sort((a, b) => 
    new Date(b.date).getTime() - new Date(a.date).getTime()
  )
  
  sorted.forEach(record => {
    const dateKey = formatDateKey(record.date)
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(record)
  })
  
  return groups
})

// 日期列表
const dateList = computed(() => {
  return Object.keys(groupedRecords.value)
})

// 是否有异常记录
const hasAbnormal = computed(() => {
  return records.value.some(r => r.isAbnormal)
})

// 最新的异常记录
const latestAbnormal = computed(() => {
  return records.value.find(r => r.isAbnormal)
})

// 格式化日期键
const formatDateKey = (date: Date | string) => {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toISOString().split('T')[0]
}

// 格式化日期显示
const formatDateDisplay = (dateKey: string) => {
  const date = new Date(dateKey)
  const today = new Date()
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  
  if (formatDateKey(date) === formatDateKey(today)) {
    return '今天'
  }
  if (formatDateKey(date) === formatDateKey(yesterday)) {
    return '昨天'
  }
  
  return date.toLocaleDateString('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short'
  })
}

// 选择日期
const selectDate = (dateKey: string) => {
  selectedDate.value = selectedDate.value === dateKey ? null : dateKey
}

onMounted(() => {
  loadRecords()
})
</script>

<template>
  <div class="status-timeline">
    <!-- 异常提醒 -->
    <AbnormalAlert 
      v-if="hasAbnormal && latestAbnormal"
      :record="latestAbnormal"
    />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="records.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <h3>暂无健康记录</h3>
      <p>机构还未上传健康记录，请稍后查看</p>
    </div>

    <!-- 时间线 -->
    <div v-else class="timeline">
      <div 
        v-for="dateKey in dateList"
        :key="dateKey"
        class="timeline-day"
      >
        <!-- 日期标题 -->
        <div 
          class="day-header"
          :class="{ expanded: selectedDate === dateKey || selectedDate === null }"
          @click="selectDate(dateKey)"
        >
          <div class="day-dot"></div>
          <span class="day-date">{{ formatDateDisplay(dateKey) }}</span>
          <span class="day-count">{{ groupedRecords[dateKey].length }} 条记录</span>
          <span class="day-toggle">
            {{ selectedDate === dateKey || selectedDate === null ? '−' : '+' }}
          </span>
        </div>

        <!-- 当天记录 -->
        <div 
          v-show="selectedDate === dateKey || selectedDate === null"
          class="day-records"
        >
          <HealthRecordCard 
            v-for="record in groupedRecords[dateKey]"
            :key="record.id"
            :record="record"
          />
        </div>
      </div>
    </div>

    <!-- 统计摘要 -->
    <div v-if="records.length > 0" class="timeline-summary">
      <div class="summary-item">
        <span class="summary-value">{{ records.length }}</span>
        <span class="summary-label">总记录数</span>
      </div>
      <div class="summary-item">
        <span class="summary-value">{{ dateList.length }}</span>
        <span class="summary-label">记录天数</span>
      </div>
      <div class="summary-item">
        <span class="summary-value" :class="{ warning: hasAbnormal }">
          {{ records.filter(r => r.isAbnormal).length }}
        </span>
        <span class="summary-label">异常记录</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.status-timeline {
  padding: 16px;
}

// 加载状态
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

// 空状态
.empty-state {
  text-align: center;
  padding: 48px 24px;
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

// 时间线
.timeline {
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: 11px;
    top: 24px;
    bottom: 24px;
    width: 2px;
    background: var(--color-border);
  }
}

.timeline-day {
  position: relative;
  margin-bottom: 24px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.day-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px 12px 0;
  cursor: pointer;
  
  &:hover {
    .day-date {
      color: var(--color-primary);
    }
  }
}

.day-dot {
  width: 24px;
  height: 24px;
  background: var(--color-primary);
  border-radius: 50%;
  border: 4px solid var(--color-surface);
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}

.day-date {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  transition: color 0.2s;
}

.day-count {
  font-size: 13px;
  color: var(--color-text-secondary);
  flex: 1;
}

.day-toggle {
  width: 24px;
  height: 24px;
  background: var(--color-surface);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--color-text-secondary);
}

.day-records {
  margin-left: 36px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

// 统计摘要
.timeline-summary {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-top: 24px;
  border: 1px solid var(--color-border);
}

.summary-item {
  text-align: center;
  
  .summary-value {
    display: block;
    font-size: 24px;
    font-weight: 700;
    color: var(--color-primary);
    
    &.warning {
      color: var(--color-warning);
    }
  }
  
  .summary-label {
    font-size: 12px;
    color: var(--color-text-secondary);
  }
}
</style>
