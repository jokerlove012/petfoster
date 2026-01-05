<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { ResponsiveLayout } from '@/layouts'
import { useAuthStore, useInstitutionStore, useNotificationStore } from '@/stores'

const authStore = useAuthStore()
const institutionStore = useInstitutionStore()
const notificationStore = useNotificationStore()

onMounted(() => {
  // 初始化各个 store
  authStore.initFromStorage()
  institutionStore.init()
  notificationStore.init()
})
</script>

<template>
  <ResponsiveLayout>
    <RouterView v-slot="{ Component }">
      <transition name="page" mode="out-in">
        <component :is="Component" />
      </transition>
    </RouterView>
  </ResponsiveLayout>
</template>

<style lang="scss">
#app {
  min-height: 100vh;
  background-color: var(--color-background);
}

// 页面过渡动画
.page-enter-active {
  animation: fadeInUp 0.3s ease-out;
}

.page-leave-active {
  animation: fadeOut 0.15s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}
</style>
