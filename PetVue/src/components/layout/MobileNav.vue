<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore, useNotificationStore } from '@/stores'

const route = useRoute()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const unreadCount = computed(() => notificationStore.unreadCount)

const navItems = computed(() => [
  { path: '/', icon: 'home', label: '首页' },
  { path: '/institutions', icon: 'search', label: '寻找' },
  { path: '/user/orders', icon: 'orders', label: '订单', auth: true },
  { path: '/notifications', icon: 'bell', label: '消息', auth: true, badge: unreadCount.value },
  { path: isAuthenticated.value ? '/user/favorites' : '/login', icon: 'user', label: isAuthenticated.value ? '我的' : '登录' }
])

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <nav class="mobile-nav">
    <router-link
      v-for="item in navItems"
      :key="item.path"
      :to="item.path"
      :class="['nav-item', { active: isActive(item.path) }]"
    >
      <div class="icon-wrapper">
        <!-- 首页图标 -->
        <svg v-if="item.icon === 'home'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
          <polyline points="9 22 9 12 15 12 15 22"></polyline>
        </svg>
        <!-- 搜索图标 -->
        <svg v-else-if="item.icon === 'search'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <!-- 订单图标 -->
        <svg v-else-if="item.icon === 'orders'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
        </svg>
        <!-- 消息图标 -->
        <svg v-else-if="item.icon === 'bell'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
        </svg>
        <!-- 用户图标 -->
        <svg v-else-if="item.icon === 'user'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        
        <span v-if="item.badge && item.badge > 0" class="badge">
          {{ item.badge > 99 ? '99+' : item.badge }}
        </span>
      </div>
      <span class="label">{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  padding: 8px 0;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 4px;
  text-decoration: none;
  color: var(--color-text-muted);
  transition: color 150ms ease;
  
  &.active {
    color: var(--color-primary);
  }
}

.icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
}

.badge {
  position: absolute;
  top: -4px;
  right: -8px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--color-error);
  color: white;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.label {
  font-size: 11px;
  font-weight: 500;
}
</style>
