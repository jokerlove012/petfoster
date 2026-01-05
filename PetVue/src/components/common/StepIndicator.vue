<template>
  <div :class="['step-indicator', `step-indicator--${direction}`]">
    <div 
      v-for="(step, index) in steps" 
      :key="index"
      :class="[
        'step-item',
        {
          'is-completed': index < currentStep,
          'is-active': index === currentStep,
          'is-pending': index > currentStep
        }
      ]"
      @click="handleStepClick(index)"
    >
      <!-- 步骤图标 -->
      <div class="step-icon">
        <template v-if="index < currentStep">
          <Check class="icon-check" />
        </template>
        <template v-else-if="step.icon">
          <span class="icon-emoji">{{ step.icon }}</span>
        </template>
        <template v-else>
          <span class="icon-number">{{ index + 1 }}</span>
        </template>
      </div>
      
      <!-- 步骤信息 -->
      <div v-if="showLabels" class="step-info">
        <span class="step-title">{{ step.title }}</span>
        <span v-if="step.description && showDescriptions" class="step-desc">
          {{ step.description }}
        </span>
      </div>
      
      <!-- 连接线 -->
      <div v-if="index < steps.length - 1" class="step-connector">
        <div class="connector-line" :class="{ filled: index < currentStep }" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Check } from 'lucide-vue-next'

interface Step {
  title: string
  description?: string
  icon?: string
}

interface Props {
  steps: Step[]
  currentStep: number
  direction?: 'horizontal' | 'vertical'
  showLabels?: boolean
  showDescriptions?: boolean
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  direction: 'horizontal',
  showLabels: true,
  showDescriptions: false,
  clickable: false
})

const emit = defineEmits<{
  (e: 'step-click', index: number): void
}>()

const handleStepClick = (index: number) => {
  if (props.clickable && index <= props.currentStep) {
    emit('step-click', index)
  }
}
</script>

<style scoped lang="scss">
.step-indicator {
  display: flex;
  
  &--horizontal {
    flex-direction: row;
    align-items: flex-start;
    
    .step-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      position: relative;
    }
    
    .step-connector {
      position: absolute;
      top: 18px;
      left: calc(50% + 20px);
      right: calc(-50% + 20px);
      height: 2px;
    }
    
    .connector-line {
      height: 100%;
      background: #e5e5e5;
      
      &.filled {
        background: var(--color-primary, #ff6b35);
      }
    }
  }
  
  &--vertical {
    flex-direction: column;
    
    .step-item {
      display: flex;
      flex-direction: row;
      align-items: flex-start;
      position: relative;
      padding-bottom: 24px;
      
      &:last-child {
        padding-bottom: 0;
      }
    }
    
    .step-info {
      margin-left: 16px;
      text-align: left;
    }
    
    .step-connector {
      position: absolute;
      top: 40px;
      left: 17px;
      width: 2px;
      height: calc(100% - 40px);
    }
    
    .connector-line {
      width: 100%;
      height: 100%;
      background: #e5e5e5;
      
      &.filled {
        background: var(--color-primary, #ff6b35);
      }
    }
  }
}

.step-item {
  cursor: default;
  
  &.is-completed,
  &.is-active {
    .step-icon {
      background: var(--color-primary, #ff6b35);
      border-color: var(--color-primary, #ff6b35);
      color: white;
    }
    
    .step-title {
      color: var(--color-text-primary, #1a1a1a);
    }
  }
  
  &.is-pending {
    .step-icon {
      background: white;
      border-color: #ddd;
      color: #999;
    }
    
    .step-title {
      color: var(--color-text-tertiary, #999);
    }
  }
  
  &.is-active {
    .step-icon {
      box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.2);
    }
  }
}

.step-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  flex-shrink: 0;
  z-index: 1;
  background: white;
  
  .icon-check {
    width: 18px;
    height: 18px;
  }
  
  .icon-emoji {
    font-size: 18px;
  }
  
  .icon-number {
    font-size: 14px;
  }
}

.step-info {
  margin-top: 12px;
  text-align: center;
}

.step-title {
  display: block;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.step-desc {
  display: block;
  font-size: 12px;
  color: var(--color-text-tertiary, #999);
  margin-top: 4px;
}

// 可点击样式
.step-indicator--horizontal .step-item,
.step-indicator--vertical .step-item {
  &.is-completed,
  &.is-active {
    cursor: pointer;
    
    &:hover .step-icon {
      transform: scale(1.1);
    }
  }
}
</style>
