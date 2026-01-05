<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-content">
      <div class="error-icon">😵</div>
      <h2>出错了</h2>
      <p>{{ errorMessage }}</p>
      <div class="error-actions">
        <AppButton type="primary" @click="retry">重试</AppButton>
        <AppButton type="outline" @click="goHome">返回首页</AppButton>
      </div>
      <details v-if="showDetails" class="error-details">
        <summary>错误详情</summary>
        <pre>{{ errorDetails }}</pre>
      </details>
    </div>
  </div>
  <slot v-else></slot>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { AppButton } from '@/components/common'

defineProps<{
  showDetails?: boolean
}>()

const router = useRouter()

const hasError = ref(false)
const errorMessage = ref('页面加载出现问题')
const errorDetails = ref('')

onErrorCaptured((err: Error) => {
  hasError.value = true
  errorMessage.value = err.message || '发生未知错误'
  errorDetails.value = err.stack || ''
  console.error('ErrorBoundary caught:', err)
  return false
})

function retry() {
  hasError.value = false
  errorMessage.value = ''
  errorDetails.value = ''
}

function goHome() {
  hasError.value = false
  router.push('/')
}
</script>

<style scoped lang="scss">
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 24px;
}

.error-content {
  text-align: center;
  max-width: 400px;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: var(--color-text-primary);
}

p {
  margin: 0 0 24px;
  color: var(--color-text-secondary);
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.error-details {
  text-align: left;
  background: var(--color-neutral-100);
  border-radius: var(--radius-md);
  padding: 12px;

  summary {
    cursor: pointer;
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 8px;
  }

  pre {
    margin: 0;
    font-size: 11px;
    color: var(--color-error);
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
