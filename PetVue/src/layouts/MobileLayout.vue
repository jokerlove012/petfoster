<script setup lang="ts">
import MobileNav from '@/components/layout/MobileNav.vue'
</script>

<template>
  <div class="mobile-layout">
    <main class="mobile-content">
      <slot></slot>
    </main>
    <MobileNav />
  </div>
</template>

<style lang="scss" scoped>
.mobile-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.mobile-content {
  flex: 1;
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}
</style>
