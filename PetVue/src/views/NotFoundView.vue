<script setup lang="ts">
import { AppButton } from '@/components/common'
</script>

<template>
  <div class="not-found-view">
    <div class="content">
      <div class="error-code">404</div>
      <h1>页面未找到</h1>
      <p>抱歉，您访问的页面不存在或已被移除。</p>
      <div class="actions">
        <router-link to="/">
          <AppButton type="primary">返回首页</AppButton>
        </router-link>
        <router-link to="/institutions">
          <AppButton type="outline">浏览机构</AppButton>
        </router-link>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.not-found-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--color-background);
}

.content {
  text-align: center;
  max-width: 400px;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: 16px;
  opacity: 0.8;
}

h1 {
  font-size: 24px;
  color: var(--color-text-primary);
  margin: 0 0 12px;
}

p {
  color: var(--color-text-secondary);
  margin: 0 0 32px;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
