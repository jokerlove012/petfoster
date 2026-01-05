<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  block?: boolean
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClass = computed(() => [
  'app-button',
  `app-button--${props.type}`,
  `app-button--${props.size}`,
  {
    'app-button--loading': props.loading,
    'app-button--disabled': props.disabled,
    'app-button--block': props.block
  }
])

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<template>
  <button
    :class="buttonClass"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="app-button__spinner"></span>
    <span v-if="icon && !loading" class="app-button__icon">
      <slot name="icon"></slot>
    </span>
    <span class="app-button__content">
      <slot></slot>
    </span>
  </button>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: var(--font-body);
  font-weight: 600;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 150ms cubic-bezier(0.33, 1, 0.68, 1);
  position: relative;
  overflow: hidden;

  // Ripple effect
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
    pointer-events: none;
  }

  &:active::after {
    animation: ripple 0.5s ease-out;
  }

  // Sizes
  &--sm {
    padding: 8px 16px;
    font-size: $text-sm;
    min-height: 32px;
  }

  &--md {
    padding: 12px 24px;
    font-size: $text-base;
    min-height: 44px;
  }

  &--lg {
    padding: 16px 32px;
    font-size: $text-lg;
    min-height: 52px;
  }

  // Types
  &--primary {
    background: var(--color-primary);
    color: white;

    &:hover:not(:disabled) {
      background: $primary-600;
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }
  }

  &--secondary {
    background: var(--color-accent);
    color: white;

    &:hover:not(:disabled) {
      background: $accent-500;
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }
  }

  &--outline {
    background: transparent;
    color: var(--color-primary);
    border: 2px solid var(--color-primary);

    &:hover:not(:disabled) {
      background: var(--color-primary-light);
    }
  }

  &--ghost {
    background: transparent;
    color: var(--color-text-primary);

    &:hover:not(:disabled) {
      background: $neutral-100;
    }
  }

  &--danger {
    background: var(--color-error);
    color: white;

    &:hover:not(:disabled) {
      background: darken(#EF4444, 10%);
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }
  }

  // States
  &--disabled,
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none !important;
  }

  &--loading {
    cursor: wait;
  }

  &--block {
    width: 100%;
  }

  // Spinner
  &__spinner {
    width: 16px;
    height: 16px;
    border: 2px solid currentColor;
    border-top-color: transparent;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  &__icon {
    display: flex;
    align-items: center;
  }

  &__content {
    display: flex;
    align-items: center;
  }
}

@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0.5;
  }
  100% {
    transform: translate(-50%, -50%) scale(4);
    opacity: 0;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
