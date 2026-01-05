<script setup lang="ts">
interface Props {
  width?: string
  height?: string
  variant?: 'text' | 'circular' | 'rectangular'
  animation?: boolean
}

withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '20px',
  variant: 'rectangular',
  animation: true
})
</script>

<template>
  <div
    :class="['skeleton', `skeleton--${variant}`, { 'skeleton--animated': animation }]"
    :style="{ width, height }"
  ></div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.skeleton {
  background: linear-gradient(
    90deg,
    #{$neutral-100} 25%,
    #{$neutral-200} 50%,
    #{$neutral-100} 75%
  );
  background-size: 200% 100%;
  
  &--rectangular {
    border-radius: var(--radius-md);
  }
  
  &--circular {
    border-radius: 50%;
  }
  
  &--text {
    border-radius: 4px;
  }
  
  &--animated {
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
