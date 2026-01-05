<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  hoverable?: boolean
  shadow?: 'none' | 'sm' | 'md' | 'lg'
  padding?: 'none' | 'sm' | 'md' | 'lg'
  bordered?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  hoverable: false,
  shadow: 'sm',
  padding: 'md',
  bordered: false
})

const cardClass = computed(() => [
  'app-card',
  `app-card--shadow-${props.shadow}`,
  `app-card--padding-${props.padding}`,
  {
    'app-card--hoverable': props.hoverable,
    'app-card--bordered': props.bordered
  }
])
</script>

<template>
  <div :class="cardClass">
    <div v-if="$slots.header" class="app-card__header">
      <slot name="header"></slot>
    </div>
    <div class="app-card__body">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="app-card__footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 300ms cubic-bezier(0.33, 1, 0.68, 1);
  display: flex;
  flex-direction: column;
  height: 100%;

  // Shadow variants
  &--shadow-none {
    box-shadow: none;
  }

  &--shadow-sm {
    box-shadow: var(--shadow-sm);
  }

  &--shadow-md {
    box-shadow: var(--shadow-md);
  }

  &--shadow-lg {
    box-shadow: var(--shadow-lg);
  }

  // Padding variants
  &--padding-none {
    .app-card__body {
      padding: 0;
    }
  }

  &--padding-sm {
    .app-card__body {
      padding: 12px;
    }
  }

  &--padding-md {
    .app-card__body {
      padding: 20px;
    }
  }

  &--padding-lg {
    .app-card__body {
      padding: 28px;
    }
  }

  // Bordered
  &--bordered {
    border: 1px solid var(--color-border);
  }

  // Hoverable
  &--hoverable {
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-lg);
    }

    &:active {
      transform: translateY(-2px);
    }
  }

  // Body - flex grow to fill space
  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  // Header
  &__header {
    padding: 16px 20px;
    border-bottom: 1px solid var(--color-border);
    font-family: var(--font-display);
    font-weight: 600;
    font-size: $text-lg;
  }

  // Footer
  &__footer {
    padding: 16px 20px;
    border-top: 1px solid var(--color-border);
    background: $neutral-50;
  }
}
</style>
