<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps<{
  institutionId: string
  blockedDates?: string[]
  bookedDates?: string[]
}>()

const emit = defineEmits<{
  (e: 'update:blockedDates', dates: string[]): void
  (e: 'save', dates: string[]): void
}>()

// 当前显示的月份
const currentMonth = ref(new Date())

// 本地管理的不可用日期
const localBlockedDates = ref<Set<string>>(new Set(props.blockedDates || []))

// 已预约日期集合（只读，不能修改）
const bookedSet = computed(() => new Set(props.bookedDates || []))

// 选择模式：'block' 或 'unblock'
const selectionMode = ref<'block' | 'unblock'>('block')

// 是否正在批量选择
const isMultiSelecting = ref(false)
const multiSelectStart = ref<Date | null>(null)

// 监听 props 变化
watch(() => props.blockedDates, (newDates) => {
  localBlockedDates.value = new Set(newDates || [])
}, { immediate: true })

// 获取月份的天数
const getDaysInMonth = (date: Date) => {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate()
}

// 获取月份第一天是星期几
const getFirstDayOfMonth = (date: Date) => {
  return new Date(date.getFullYear(), date.getMonth(), 1).getDay()
}

// 格式化日期字符串
const formatDateStr = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 生成日历数据
const calendarDays = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  const daysInMonth = getDaysInMonth(currentMonth.value)
  const firstDay = getFirstDayOfMonth(currentMonth.value)
  
  const days: Array<{
    date: Date
    dateStr: string
    day: number
    isCurrentMonth: boolean
    isToday: boolean
    isPast: boolean
    isBlocked: boolean
    isBooked: boolean
  }> = []
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  // 上个月的天数
  const prevMonth = new Date(year, month, 0)
  const prevMonthDays = prevMonth.getDate()
  
  // 填充上个月的日期
  for (let i = firstDay - 1; i >= 0; i--) {
    const day = prevMonthDays - i
    const date = new Date(year, month - 1, day)
    const dateStr = formatDateStr(date)
    days.push({
      date,
      dateStr,
      day,
      isCurrentMonth: false,
      isToday: false,
      isPast: date < today,
      isBlocked: localBlockedDates.value.has(dateStr),
      isBooked: bookedSet.value.has(dateStr)
    })
  }
  
  // 当前月的日期
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    const dateStr = formatDateStr(date)
    const isPast = date < today
    
    days.push({
      date,
      dateStr,
      day,
      isCurrentMonth: true,
      isToday: date.getTime() === today.getTime(),
      isPast,
      isBlocked: localBlockedDates.value.has(dateStr),
      isBooked: bookedSet.value.has(dateStr)
    })
  }
  
  // 填充下个月的日期
  const remainingDays = 42 - days.length
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(year, month + 1, day)
    const dateStr = formatDateStr(date)
    days.push({
      date,
      dateStr,
      day,
      isCurrentMonth: false,
      isToday: false,
      isPast: false,
      isBlocked: localBlockedDates.value.has(dateStr),
      isBooked: bookedSet.value.has(dateStr)
    })
  }
  
  return days
})

// 月份导航
const prevMonthNav = () => {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() - 1,
    1
  )
}

const nextMonthNav = () => {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() + 1,
    1
  )
}

// 格式化月份显示
const monthDisplay = computed(() => {
  return currentMonth.value.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long'
  })
})

// 点击日期
const handleDayClick = (dayInfo: typeof calendarDays.value[0]) => {
  if (!dayInfo.isCurrentMonth || dayInfo.isPast) return
  
  // 已预约的日期不能修改
  if (dayInfo.isBooked) {
    ElMessage.warning('该日期已有预约，无法修改')
    return
  }
  
  const dateStr = dayInfo.dateStr
  
  if (selectionMode.value === 'block') {
    if (!localBlockedDates.value.has(dateStr)) {
      localBlockedDates.value.add(dateStr)
      emitUpdate()
    }
  } else {
    if (localBlockedDates.value.has(dateStr)) {
      localBlockedDates.value.delete(dateStr)
      emitUpdate()
    }
  }
}

// 批量选择开始
const handleDayMouseDown = (dayInfo: typeof calendarDays.value[0]) => {
  if (!dayInfo.isCurrentMonth || dayInfo.isPast || dayInfo.isBooked) return
  isMultiSelecting.value = true
  multiSelectStart.value = dayInfo.date
}

// 批量选择结束
const handleDayMouseUp = (dayInfo: typeof calendarDays.value[0]) => {
  if (!isMultiSelecting.value || !multiSelectStart.value) return
  
  if (!dayInfo.isCurrentMonth || dayInfo.isPast) {
    isMultiSelecting.value = false
    multiSelectStart.value = null
    return
  }
  
  const startDate = multiSelectStart.value
  const endDate = dayInfo.date
  
  // 确保开始日期在结束日期之前
  const [from, to] = startDate <= endDate ? [startDate, endDate] : [endDate, startDate]
  
  // 批量更新日期
  const current = new Date(from)
  while (current <= to) {
    const dateStr = formatDateStr(current)
    if (!bookedSet.value.has(dateStr) && current >= new Date(new Date().setHours(0, 0, 0, 0))) {
      if (selectionMode.value === 'block') {
        localBlockedDates.value.add(dateStr)
      } else {
        localBlockedDates.value.delete(dateStr)
      }
    }
    current.setDate(current.getDate() + 1)
  }
  
  isMultiSelecting.value = false
  multiSelectStart.value = null
  emitUpdate()
}

// 发送更新事件
const emitUpdate = () => {
  const dates = Array.from(localBlockedDates.value).sort()
  emit('update:blockedDates', dates)
}

// 保存更改
const handleSave = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要保存可用性设置吗？这将影响用户的预约选择。',
      '确认保存',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const dates = Array.from(localBlockedDates.value).sort()
    emit('save', dates)
    ElMessage.success('可用性设置已保存')
  } catch {
    // 用户取消
  }
}

// 清除所有不可用日期
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清除所有不可用日期设置吗？',
      '确认清除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    localBlockedDates.value.clear()
    emitUpdate()
    ElMessage.success('已清除所有不可用日期')
  } catch {
    // 用户取消
  }
}

// 快速设置：本月剩余日期全部不可用
const blockRestOfMonth = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  const daysInMonth = getDaysInMonth(currentMonth.value)
  
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    if (date >= today) {
      const dateStr = formatDateStr(date)
      if (!bookedSet.value.has(dateStr)) {
        localBlockedDates.value.add(dateStr)
      }
    }
  }
  emitUpdate()
  ElMessage.success('已设置本月剩余日期为不可用')
}

// 统计信息
const stats = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  const daysInMonth = getDaysInMonth(currentMonth.value)
  
  let available = 0
  let blocked = 0
  let booked = 0
  
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    if (date >= today) {
      const dateStr = formatDateStr(date)
      if (bookedSet.value.has(dateStr)) {
        booked++
      } else if (localBlockedDates.value.has(dateStr)) {
        blocked++
      } else {
        available++
      }
    }
  }
  
  return { available, blocked, booked }
})

// 星期标题
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
</script>

<template>
  <div class="availability-calendar">
    <!-- 操作栏 -->
    <div class="calendar-toolbar">
      <div class="mode-switch">
        <button 
          class="mode-btn"
          :class="{ active: selectionMode === 'block' }"
          @click="selectionMode = 'block'"
        >
          <span class="mode-icon">🚫</span>
          设为不可用
        </button>
        <button 
          class="mode-btn"
          :class="{ active: selectionMode === 'unblock' }"
          @click="selectionMode = 'unblock'"
        >
          <span class="mode-icon">✓</span>
          设为可用
        </button>
      </div>
      
      <div class="quick-actions">
        <button class="action-btn" @click="blockRestOfMonth">
          本月全部不可用
        </button>
        <button class="action-btn danger" @click="handleClearAll">
          清除设置
        </button>
      </div>
    </div>
    
    <!-- 日历头部 -->
    <div class="calendar-header">
      <button class="nav-btn" @click="prevMonthNav">‹</button>
      <span class="month-display">{{ monthDisplay }}</span>
      <button class="nav-btn" @click="nextMonthNav">›</button>
    </div>
    
    <!-- 星期标题 -->
    <div class="weekday-header">
      <span v-for="day in weekDays" :key="day" class="weekday">{{ day }}</span>
    </div>
    
    <!-- 日历网格 -->
    <div class="calendar-grid">
      <div 
        v-for="(dayInfo, index) in calendarDays" 
        :key="index"
        class="calendar-day"
        :class="{
          'other-month': !dayInfo.isCurrentMonth,
          'today': dayInfo.isToday,
          'past': dayInfo.isPast,
          'blocked': dayInfo.isBlocked && !dayInfo.isBooked,
          'booked': dayInfo.isBooked
        }"
        @click="handleDayClick(dayInfo)"
        @mousedown="handleDayMouseDown(dayInfo)"
        @mouseup="handleDayMouseUp(dayInfo)"
      >
        <span class="day-number">{{ dayInfo.day }}</span>
        <span v-if="dayInfo.isToday" class="today-indicator">今</span>
        <span v-if="dayInfo.isBooked" class="status-badge booked">已约</span>
        <span v-else-if="dayInfo.isBlocked && dayInfo.isCurrentMonth && !dayInfo.isPast" class="status-badge blocked">休</span>
      </div>
    </div>
    
    <!-- 图例 -->
    <div class="calendar-legend">
      <div class="legend-item">
        <span class="legend-dot available"></span>
        <span>可预约</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot blocked"></span>
        <span>不可用</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot booked"></span>
        <span>已预约</span>
      </div>
    </div>
    
    <!-- 统计信息 -->
    <div class="calendar-stats">
      <div class="stat-item">
        <span class="stat-value available">{{ stats.available }}</span>
        <span class="stat-label">可预约</span>
      </div>
      <div class="stat-item">
        <span class="stat-value blocked">{{ stats.blocked }}</span>
        <span class="stat-label">不可用</span>
      </div>
      <div class="stat-item">
        <span class="stat-value booked">{{ stats.booked }}</span>
        <span class="stat-label">已预约</span>
      </div>
    </div>
    
    <!-- 保存按钮 -->
    <div class="calendar-actions">
      <button class="save-btn" @click="handleSave">
        保存设置
      </button>
    </div>
    
    <!-- 提示信息 -->
    <div class="calendar-tips">
      <p>💡 提示：点击日期可切换可用状态，拖动可批量选择</p>
      <p>⚠️ 已有预约的日期无法修改</p>
    </div>
  </div>
</template>


<style lang="scss" scoped>
@import '@/styles/variables.scss';

.availability-calendar {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  max-width: 420px;
}

// 工具栏
.calendar-toolbar {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.mode-switch {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.mode-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 12px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  
  .mode-icon {
    font-size: 14px;
  }
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
  
  &.active {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
    color: var(--color-primary);
  }
}

.quick-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-neutral-100);
  }
  
  &.danger {
    color: var(--color-error);
    border-color: var(--color-error);
    
    &:hover {
      background: rgba(239, 68, 68, 0.1);
    }
  }
}

// 日历头部
.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.nav-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--color-surface);
  border-radius: 50%;
  font-size: 18px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-primary-light);
    color: var(--color-primary);
  }
}

.month-display {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

// 星期标题
.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}

.weekday {
  text-align: center;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-secondary);
  padding: 6px 0;
}

// 日历网格
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 3px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  min-height: 44px;
  background: var(--color-surface);
  border: 1px solid transparent;
  
  .day-number {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);
  }
  
  .today-indicator {
    position: absolute;
    top: 2px;
    right: 2px;
    font-size: 9px;
    color: var(--color-primary);
    font-weight: 600;
  }
  
  .status-badge {
    position: absolute;
    bottom: 2px;
    font-size: 9px;
    font-weight: 500;
    padding: 1px 3px;
    border-radius: 2px;
    
    &.blocked {
      background: var(--color-neutral-200);
      color: var(--color-text-secondary);
    }
    
    &.booked {
      background: var(--color-accent);
      color: white;
    }
  }
  
  &:hover:not(.other-month):not(.past):not(.booked) {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
  
  &.other-month {
    .day-number {
      color: var(--color-text-tertiary);
    }
    cursor: default;
    background: var(--color-neutral-50);
  }
  
  &.today {
    border-color: var(--color-primary);
    
    .day-number {
      color: var(--color-primary);
      font-weight: 700;
    }
  }
  
  &.past {
    cursor: not-allowed;
    background: var(--color-neutral-100);
    
    .day-number {
      color: var(--color-text-tertiary);
    }
  }
  
  &.blocked {
    background: var(--color-neutral-200);
    
    .day-number {
      color: var(--color-text-secondary);
    }
  }
  
  &.booked {
    background: linear-gradient(135deg, var(--color-accent-light) 0%, var(--color-accent) 100%);
    cursor: not-allowed;
    
    .day-number {
      color: white;
      font-weight: 600;
    }
  }
}

// 图例
.calendar-legend {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  
  &.available {
    background: var(--color-surface);
    border: 1px solid var(--color-border);
  }
  
  &.blocked {
    background: var(--color-neutral-200);
  }
  
  &.booked {
    background: var(--color-accent);
  }
}

// 统计信息
.calendar-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
  padding: 12px;
  background: var(--color-neutral-50);
  border-radius: var(--radius-md);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  
  &.available {
    color: var(--color-success);
  }
  
  &.blocked {
    color: var(--color-text-secondary);
  }
  
  &.booked {
    color: var(--color-accent);
  }
}

.stat-label {
  font-size: 11px;
  color: var(--color-text-secondary);
}

// 保存按钮
.calendar-actions {
  margin-top: 16px;
}

.save-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 14px 0 rgba(255, 107, 53, 0.39);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px 0 rgba(255, 107, 53, 0.5);
  }
  
  &:active {
    transform: translateY(0);
  }
}

// 提示信息
.calendar-tips {
  margin-top: 12px;
  padding: 10px;
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
  
  p {
    font-size: 11px;
    color: var(--color-text-secondary);
    margin: 0;
    line-height: 1.6;
    
    &:not(:last-child) {
      margin-bottom: 4px;
    }
  }
}
</style>
