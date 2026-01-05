<template>
  <div class="feature-tooltip-wrapper" ref="wrapperRef">
    <slot />
    
    <Teleport to="body">
      <Transition name="tooltip-fade">
        <div 
          v-if="visible && !dismissed"
          class="feature-tooltip"
          :class="[`tooltip-${position}`]"
          :style="tooltipStyle"
          ref="tooltipRef"
        >
          <div class="tooltip-arrow" />
          <div class="tooltip-content">
            <div v-if="icon" class="tooltip-icon">{{ icon }}</div>
            <div class="tooltip-body">
              <h4 v-if="title">{{ title }}</h4>
              <p>{{ content }}</p>
            </div>
            <button class="tooltip-close" @click="dismiss">×</button>
          </div>
          <div v-if="showActions" class="tooltip-actions">
            <button class="action-btn secondary" @click="dismiss">知道了</button>
            <button v-if="nextStep" class="action-btn primary" @click="goNext">
              下一步
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

interface Props {
  featureKey: string
  title?: string
  content: string
  icon?: string
  position?: 'top' | 'bottom' | 'left' | 'right'
  showOnce?: boolean
  showActions?: boolean
  nextStep?: string
  delay?: number
  trigger?: 'auto' | 'hover' | 'click'
}

const props = withDefaults(defineProps<Props>(), {
  position: 'bottom',
  showOnce: true,
  showActions: true,
  delay: 500,
  trigger: 'auto'
})

const emit = defineEmits<{
  (e: 'dismiss'): void
  (e: 'next', nextKey: string): void
}>()

const wrapperRef = ref<HTMLElement>()
const tooltipRef = ref<HTMLElement>()
const visible = ref(false)
const dismissed = ref(false)
const tooltipPosition = ref({ top: 0, left: 0 })

const storageKey = computed(() => `feature_tooltip_${props.featureKey}`)

const tooltipStyle = computed(() => ({
  top: `${tooltipPosition.value.top}px`,
  left: `${tooltipPosition.value.left}px`
}))

const checkDismissed = () => {
  if (props.showOnce) {
    const stored = localStorage.getItem(storageKey.value)
    dismissed.value = stored === 'dismissed'
  }
}

const calculatePosition = () => {
  if (!wrapperRef.value || !tooltipRef.value) return
  
  const wrapperRect = wrapperRef.value.getBoundingClientRect()
  const tooltipRect = tooltipRef.value.getBoundingClientRect()
  const gap = 12
  
  let top = 0
  let left = 0
  
  switch (props.position) {
    case 'top':
      top = wrapperRect.top - tooltipRect.height - gap
      left = wrapperRect.left + (wrapperRect.width - tooltipRect.width) / 2
      break
    case 'bottom':
      top = wrapperRect.bottom + gap
      left = wrapperRect.left + (wrapperRect.width - tooltipRect.width) / 2
      break
    case 'left':
      top = wrapperRect.top + (wrapperRect.height - tooltipRect.height) / 2
      left = wrapperRect.left - tooltipRect.width - gap
      break
    case 'right':
      top = wrapperRect.top + (wrapperRect.height - tooltipRect.height) / 2
      left = wrapperRect.right + gap
      break
  }
  
  // 边界检测
  const padding = 16
  left = Math.max(padding, Math.min(left, window.innerWidth - tooltipRect.width - padding))
  top = Math.max(padding, Math.min(top, window.innerHeight - tooltipRect.height - padding))
  
  tooltipPosition.value = { top, left }
}

const show = () => {
  if (dismissed.value) return
  visible.value = true
  nextTick(() => {
    calculatePosition()
  })
}

const hide = () => {
  visible.value = false
}

const dismiss = () => {
  if (props.showOnce) {
    localStorage.setItem(storageKey.value, 'dismissed')
  }
  dismissed.value = true
  visible.value = false
  emit('dismiss')
}

const goNext = () => {
  dismiss()
  if (props.nextStep) {
    emit('next', props.nextStep)
  }
}

// 事件处理
const handleMouseEnter = () => {
  if (props.trigger === 'hover') {
    show()
  }
}

const handleMouseLeave = () => {
  if (props.trigger === 'hover') {
    hide()
  }
}

const handleClick = () => {
  if (props.trigger === 'click') {
    visible.value ? hide() : show()
  }
}

// 窗口大小变化时重新计算位置
const handleResize = () => {
  if (visible.value) {
    calculatePosition()
  }
}

watch(visible, (val) => {
  if (val) {
    nextTick(() => calculatePosition())
  }
})

onMounted(() => {
  checkDismissed()
  
  if (props.trigger === 'auto' && !dismissed.value) {
    setTimeout(() => {
      show()
    }, props.delay)
  }
  
  window.addEventListener('resize', handleResize)
  window.addEventListener('scroll', handleResize, true)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('scroll', handleResize, true)
})

defineExpose({ show, hide, dismiss })
</script>

<style scoped lang="scss">
.feature-tooltip-wrapper {
  display: inline-block;
  position: relative;
}

.feature-tooltip {
  position: fixed;
  z-index: 9999;
  width: 280px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.tooltip-arrow {
  position: absolute;
  width: 12px;
  height: 12px;
  background: white;
  transform: rotate(45deg);
}

.tooltip-top .tooltip-arrow {
  bottom: -6px;
  left: 50%;
  margin-left: -6px;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.tooltip-bottom .tooltip-arrow {
  top: -6px;
  left: 50%;
  margin-left: -6px;
  box-shadow: -2px -2px 4px rgba(0, 0, 0, 0.1);
}

.tooltip-left .tooltip-arrow {
  right: -6px;
  top: 50%;
  margin-top: -6px;
  box-shadow: 2px -2px 4px rgba(0, 0, 0, 0.1);
}

.tooltip-right .tooltip-arrow {
  left: -6px;
  top: 50%;
  margin-top: -6px;
  box-shadow: -2px 2px 4px rgba(0, 0, 0, 0.1);
}

.tooltip-content {
  display: flex;
  gap: 12px;
  padding: 16px;
  position: relative;
}

.tooltip-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.tooltip-body {
  flex: 1;
  
  h4 {
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 6px;
    color: var(--color-text-primary, #1a1a1a);
    padding-right: 20px;
  }
  
  p {
    font-size: 13px;
    line-height: 1.5;
    color: var(--color-text-secondary, #666);
    margin: 0;
  }
}

.tooltip-close {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  
  &:hover {
    background: #f5f5f5;
    color: #666;
  }
}

.tooltip-actions {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #f9f9f9;
  border-top: 1px solid #eee;
}

.action-btn {
  flex: 1;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &.secondary {
    background: white;
    border: 1px solid #ddd;
    color: #666;
    
    &:hover {
      border-color: #bbb;
    }
  }
  
  &.primary {
    background: var(--color-primary, #ff6b35);
    border: none;
    color: white;
    
    &:hover {
      background: #e55a2b;
    }
  }
}

// 过渡动画
.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: all 0.2s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
