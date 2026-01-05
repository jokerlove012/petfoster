<template>
  <div :class="['success-feedback', { 'is-visible': visible }]">
    <div class="feedback-overlay" @click="handleOverlayClick" />
    
    <div class="feedback-content">
      <div class="feedback-icon" :class="[`feedback-icon--${type}`]">
        <Transition name="icon-pop" appear>
          <component :is="iconComponent" class="icon" />
        </Transition>
      </div>
      
      <h2 class="feedback-title">{{ title }}</h2>
      <p v-if="description" class="feedback-description">{{ description }}</p>
      
      <div v-if="details?.length" class="feedback-details">
        <div v-for="(detail, index) in details" :key="index" class="detail-item">
          <span class="detail-label">{{ detail.label }}</span>
          <span class="detail-value">{{ detail.value }}</span>
        </div>
      </div>
      
      <div v-if="nextSteps?.length" class="feedback-next-steps">
        <p class="next-steps-title">接下来您可以：</p>
        <div class="next-steps-list">
          <button 
            v-for="(step, index) in nextSteps" 
            :key="index"
            class="next-step-btn"
            @click="handleStepClick(step)"
          >
            <span v-if="step.icon" class="step-icon">{{ step.icon }}</span>
            <span class="step-text">{{ step.text }}</span>
          </button>
        </div>
      </div>
      
      <div class="feedback-actions">
        <slot name="actions">
          <button 
            v-if="primaryAction" 
            class="action-btn primary"
            @click="handlePrimaryAction"
          >
            {{ primaryAction }}
          </button>
          <button 
            v-if="secondaryAction" 
            class="action-btn secondary"
            @click="handleSecondaryAction"
          >
            {{ secondaryAction }}
          </button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { CheckCircle, AlertCircle, Info, XCircle } from 'lucide-vue-next'

type FeedbackType = 'success' | 'warning' | 'info' | 'error'

interface DetailItem {
  label: string
  value: string
}

interface NextStep {
  icon?: string
  text: string
  action: string
}

interface Props {
  visible: boolean
  type?: FeedbackType
  title: string
  description?: string
  details?: DetailItem[]
  nextSteps?: NextStep[]
  primaryAction?: string
  secondaryAction?: string
  autoClose?: number
  closeOnOverlay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'success',
  autoClose: 0,
  closeOnOverlay: true
})

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'primary'): void
  (e: 'secondary'): void
  (e: 'step', action: string): void
}>()

const iconComponent = computed(() => {
  const icons = {
    success: CheckCircle,
    warning: AlertCircle,
    info: Info,
    error: XCircle
  }
  return icons[props.type]
})

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    emit('close')
  }
}

const handlePrimaryAction = () => {
  emit('primary')
}

const handleSecondaryAction = () => {
  emit('secondary')
}

const handleStepClick = (step: NextStep) => {
  emit('step', step.action)
}

// 自动关闭
watch(() => props.visible, (val) => {
  if (val && props.autoClose > 0) {
    setTimeout(() => {
      emit('close')
    }, props.autoClose)
  }
})
</script>

<style scoped lang="scss">
.success-feedback {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  
  &.is-visible {
    opacity: 1;
    visibility: visible;
    
    .feedback-content {
      transform: scale(1) translateY(0);
    }
  }
}

.feedback-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.feedback-content {
  position: relative;
  width: 90%;
  max-width: 420px;
  background: white;
  border-radius: 20px;
  padding: 40px 32px;
  text-align: center;
  transform: scale(0.9) translateY(20px);
  transition: transform 0.3s ease;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.feedback-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .icon {
    width: 48px;
    height: 48px;
  }
  
  &--success {
    background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
    color: #28a745;
  }
  
  &--warning {
    background: linear-gradient(135deg, #fff3cd 0%, #ffeeba 100%);
    color: #ffc107;
  }
  
  &--info {
    background: linear-gradient(135deg, #d1ecf1 0%, #bee5eb 100%);
    color: #17a2b8;
  }
  
  &--error {
    background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
    color: #dc3545;
  }
}

.feedback-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary, #1a1a1a);
  margin: 0 0 12px;
}

.feedback-description {
  font-size: 15px;
  color: var(--color-text-secondary, #666);
  line-height: 1.6;
  margin: 0;
}

.feedback-details {
  margin-top: 24px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  
  &:not(:last-child) {
    border-bottom: 1px solid #eee;
  }
}

.detail-label {
  font-size: 14px;
  color: var(--color-text-secondary, #666);
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #1a1a1a);
}

.feedback-next-steps {
  margin-top: 24px;
  text-align: left;
}

.next-steps-title {
  font-size: 13px;
  color: var(--color-text-tertiary, #999);
  margin: 0 0 12px;
}

.next-steps-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.next-step-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f5f5f5;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: #eee;
  }
  
  .step-icon {
    font-size: 18px;
  }
  
  .step-text {
    font-size: 14px;
    color: var(--color-text-primary, #1a1a1a);
  }
}

.feedback-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}

.action-btn {
  flex: 1;
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &.primary {
    background: var(--color-primary, #ff6b35);
    border: none;
    color: white;
    
    &:hover {
      background: #e55a2b;
    }
  }
  
  &.secondary {
    background: white;
    border: 2px solid #ddd;
    color: var(--color-text-secondary, #666);
    
    &:hover {
      border-color: #bbb;
    }
  }
}

// 图标弹出动画
.icon-pop-enter-active {
  animation: iconPop 0.5s ease;
}

@keyframes iconPop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
