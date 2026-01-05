<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Calendar } from 'lucide-vue-next'

const props = defineProps<{
  institutionId: string
  minDate?: Date
  maxDate?: Date
  unavailableDates?: string[]
}>()

const emit = defineEmits<{
  (e: 'select', dates: { start: Date; end: Date }): void
}>()

// 日期范围
const dateRange = ref<[Date, Date] | null>(null)

// 不可用日期集合
const unavailableSet = computed(() => {
  return new Set(props.unavailableDates || [])
})

// 禁用日期（过去的日期和不可用日期）
const disabledDate = (date: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  if (date < today) return true
  
  const dateStr = date.toISOString().split('T')[0]
  return unavailableSet.value.has(dateStr)
}

// 快捷选项
const shortcuts = [
  {
    text: '今天起3天',
    value: () => {
      const start = new Date()
      const end = new Date()
      end.setDate(end.getDate() + 2)
      return [start, end]
    }
  },
  {
    text: '今天起7天',
    value: () => {
      const start = new Date()
      const end = new Date()
      end.setDate(end.getDate() + 6)
      return [start, end]
    }
  },
  {
    text: '今天起14天',
    value: () => {
      const start = new Date()
      const end = new Date()
      end.setDate(end.getDate() + 13)
      return [start, end]
    }
  },
  {
    text: '今天起30天',
    value: () => {
      const start = new Date()
      const end = new Date()
      end.setDate(end.getDate() + 29)
      return [start, end]
    }
  },
  {
    text: '下周',
    value: () => {
      const today = new Date()
      const dayOfWeek = today.getDay()
      const daysUntilNextMonday = dayOfWeek === 0 ? 1 : 8 - dayOfWeek
      const start = new Date()
      start.setDate(today.getDate() + daysUntilNextMonday)
      const end = new Date(start)
      end.setDate(start.getDate() + 6)
      return [start, end]
    }
  },
  {
    text: '下个月',
    value: () => {
      const today = new Date()
      const start = new Date(today.getFullYear(), today.getMonth() + 1, 1)
      const end = new Date(today.getFullYear(), today.getMonth() + 2, 0)
      return [start, end]
    }
  }
]

// 计算天数
const totalDays = computed(() => {
  if (!dateRange.value || !dateRange.value[0] || !dateRange.value[1]) return 0
  const start = new Date(dateRange.value[0])
  const end = new Date(dateRange.value[1])
  const diffTime = Math.abs(end.getTime() - start.getTime())
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
})

// 监听日期变化
watch(dateRange, (newVal) => {
  if (newVal && newVal[0] && newVal[1]) {
    emit('select', {
      start: newVal[0],
      end: newVal[1]
    })
  }
})

// 格式化日期显示
const formatDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short'
  })
}
</script>

<template>
  <div class="booking-calendar">
    <!-- 日期选择器 -->
    <div class="date-picker-wrapper">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="入住日期"
        end-placeholder="离店日期"
        :disabled-date="disabledDate"
        :shortcuts="shortcuts"
        format="YYYY年MM月DD日"
        value-format="YYYY-MM-DD"
        :clearable="true"
        size="large"
        style="width: 100%"
        popper-class="booking-date-popper"
        :teleported="false"
      />
    </div>

    <!-- 选择结果展示 -->
    <div v-if="dateRange && dateRange[0] && dateRange[1]" class="date-result">
      <div class="result-card">
        <div class="result-item">
          <div class="result-label">
            <Calendar class="label-icon" />
            入住日期
          </div>
          <div class="result-value">{{ formatDate(new Date(dateRange[0])) }}</div>
        </div>
        <div class="result-arrow">→</div>
        <div class="result-item">
          <div class="result-label">
            <Calendar class="label-icon" />
            离店日期
          </div>
          <div class="result-value">{{ formatDate(new Date(dateRange[1])) }}</div>
        </div>
      </div>
      <div class="result-summary">
        共 <span class="days-count">{{ totalDays }}</span> 天
      </div>
    </div>

    <!-- 提示信息 -->
    <div v-else class="date-hint">
      <Calendar class="hint-icon" />
      <p>请选择入住和离店日期</p>
      <p class="hint-sub">点击上方日期框选择，或使用快捷选项</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.booking-calendar {
  width: 100%;
}

.date-picker-wrapper {
  margin-bottom: 20px;

  :deep(.el-date-editor) {
    width: 100% !important;
    
    .el-range-input {
      font-size: 15px;
    }
    
    .el-range-separator {
      color: var(--color-primary);
      font-weight: 500;
    }
  }
}

.date-result {
  background: linear-gradient(135deg, var(--color-primary-light), #fff5f0);
  border-radius: var(--radius-lg);
  padding: 20px;
  border: 1px solid var(--color-primary-light);
}

.result-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.result-item {
  flex: 1;
  text-align: center;
}

.result-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;

  .label-icon {
    width: 14px;
    height: 14px;
  }
}

.result-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.result-arrow {
  font-size: 20px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.result-summary {
  text-align: center;
  padding-top: 16px;
  border-top: 1px dashed var(--color-border);
  font-size: 14px;
  color: var(--color-text-secondary);

  .days-count {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-primary);
    margin: 0 4px;
  }
}

.date-hint {
  text-align: center;
  padding: 32px 20px;
  background: #f9fafb;
  border-radius: var(--radius-lg);
  border: 2px dashed var(--color-border);

  .hint-icon {
    width: 40px;
    height: 40px;
    color: var(--color-text-tertiary);
    margin-bottom: 12px;
  }

  p {
    margin: 0;
    color: var(--color-text-secondary);
    font-size: 14px;
  }

  .hint-sub {
    font-size: 12px;
    color: var(--color-text-tertiary);
    margin-top: 4px;
  }
}

@media (max-width: 480px) {
  .result-card {
    flex-direction: column;
    gap: 12px;
  }

  .result-arrow {
    transform: rotate(90deg);
  }
}
</style>

<style lang="scss">
// 全局样式 - 日期选择器弹出框
.booking-date-popper {
  .el-picker-panel__shortcut {
    font-size: 13px;
    padding: 8px 12px;
    
    &:hover {
      color: var(--color-primary);
      background: var(--color-primary-light);
    }
  }
  
  .el-date-range-picker__content {
    padding: 12px;
  }
  
  .el-date-table td.today .el-date-table-cell__text {
    color: var(--color-primary);
    font-weight: 700;
  }
  
  .el-date-table td.in-range .el-date-table-cell__text {
    background: var(--color-primary-light);
  }
  
  .el-date-table td.start-date .el-date-table-cell__text,
  .el-date-table td.end-date .el-date-table-cell__text {
    background: var(--color-primary);
  }
}
</style>
