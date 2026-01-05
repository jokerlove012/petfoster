<script setup lang="ts">
import Skeleton from '@/components/common/Skeleton.vue'
</script>

<template>
  <div class="institution-card-skeleton">
    <Skeleton class="image" height="160px" />
    <div class="content">
      <Skeleton class="title" height="24px" width="70%" />
      <Skeleton class="rating" height="16px" width="40%" />
      <Skeleton class="address" height="16px" width="90%" />
      <div class="footer">
        <Skeleton class="price" height="20px" width="30%" />
        <Skeleton class="distance" height="16px" width="20%" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.institution-card-skeleton {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}
</style>
