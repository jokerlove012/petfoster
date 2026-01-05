<template>
  <div class="care-tip-card" :class="tip.category">
    <div class="tip-icon">{{ categoryIcon }}</div>
    <div class="tip-content">
      <h4>{{ tip.title }}</h4>
      <p>{{ tip.content }}</p>
    </div>
    <button v-if="showAction" class="tip-action" @click="$emit('action')">
      <ChevronRight :size="16" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import type { CareTip } from '@/types/ai'

const props = defineProps<{
  tip: CareTip
  showAction?: boolean
}>()

defineEmits<{
  (e: 'action'): void
}>()

const categoryIcon = computed(() => {
  const icons: Record<string, string> = {
    feeding: '🍖',
    grooming: '✨',
    health: '💊',
    exercise: '🏃',
    behavior: '🧠',
    general: '💡'
  }
  return props.tip.icon || icons[props.tip.category] || '💡'
})
</script>

<style scoped lang="scss">
.care-tip-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  transition: all 0.2s;

  &:hover {
    box-shadow: var(--shadow-md);
  }

  // 分类颜色
  &.feeding {
    border-left: 3px solid #f59e0b;
    background: linear-gradient(90deg, #fffbeb 0%, var(--color-surface) 100%);
  }

  &.grooming {
    border-left: 3px solid #ec4899;
    background: linear-gradient(90deg, #fdf2f8 0%, var(--color-surface) 100%);
  }

  &.health {
    border-left: 3px solid #22c55e;
    background: linear-gradient(90deg, #f0fdf4 0%, var(--color-surface) 100%);
  }

  &.exercise {
    border-left: 3px solid #3b82f6;
    background: linear-gradient(90deg, #eff6ff 0%, var(--color-surface) 100%);
  }

  &.behavior {
    border-left: 3px solid #8b5cf6;
    background: linear-gradient(90deg, #f5f3ff 0%, var(--color-surface) 100%);
  }

  &.general {
    border-left: 3px solid var(--color-primary);
    background: linear-gradient(
      90deg,
      var(--color-primary-light) 0%,
      var(--color-surface) 100%
    );
  }
}

.tip-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.tip-content {
  flex: 1;
  min-width: 0;

  h4 {
    margin: 0 0 4px;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text-primary);
  }

  p {
    margin: 0;
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.tip-action {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 50%;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--color-neutral-100);
    color: var(--color-primary);
  }
}
</style>
