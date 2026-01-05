<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, useNotificationStore } from '@/stores'
import { AppButton } from '@/components/common'
import UserDropdown from './UserDropdown.vue'
import NotificationDropdown from './NotificationDropdown.vue'
import SearchOverlay from './SearchOverlay.vue'
import { Wallet } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const userRole = computed(() => authStore.userRole)
const showSearch = ref(false)

// 根据角色获取管理后台路径
const dashboardPath = computed(() => {
  if (userRole.value === 'admin') return '/admin/dashboard'
  if (userRole.value === 'institution_staff') return '/institution/dashboard'
  return null
})

// 根据角色获取管理后台名称
const dashboardLabel = computed(() => {
  if (userRole.value === 'admin') return '管理后台'
  if (userRole.value === 'institution_staff') return '机构管理'
  return ''
})

const openSearch = () => {
  router.push('/ai/chat')
}

const closeSearch = () => {
  showSearch.value = false
}

const goToWallet = () => {
  router.push('/wallet')
}
</script>

<template>
  <header class="app-header">
    <div class="header-content">
      <router-link to="/" class="logo">
        <span class="logo-icon">🐾</span>
        <span class="logo-text">宠物寄养</span>
      </router-link>

      <nav class="nav-links">
        <router-link to="/institutions" class="nav-link">寻找机构</router-link>
        <router-link to="/help" class="nav-link">帮助中心</router-link>
        <!-- 管理后台入口 - 仅管理员和机构显示 -->
        <router-link 
          v-if="isAuthenticated && dashboardPath" 
          :to="dashboardPath" 
          class="nav-link"
        >
          {{ dashboardLabel }}
        </router-link>
      </nav>

      <div class="header-actions">
        <!-- 搜索按钮 -->
        <button class="icon-btn" @click="openSearch" title="搜索">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
        </button>

        <template v-if="isAuthenticated && user">
          <!-- 钱包按钮 -->
          <button class="icon-btn" @click="goToWallet" title="我的钱包">
            <Wallet :size="20" />
          </button>

          <!-- 管理后台按钮 - 移动端显示 -->
          <router-link v-if="dashboardPath" :to="dashboardPath" class="dashboard-btn-mobile">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
          </router-link>

          <!-- 通知下拉 -->
          <NotificationDropdown />

          <!-- 用户下拉 -->
          <UserDropdown :user="user" />
        </template>

        <template v-else>
          <router-link to="/login">
            <AppButton type="ghost" size="sm">登录</AppButton>
          </router-link>
          <router-link to="/register">
            <AppButton type="primary" size="sm">注册</AppButton>
          </router-link>
        </template>
      </div>
    </div>

    <!-- 搜索覆盖层 -->
    <SearchOverlay :visible="showSearch" @close="closeSearch" />
  </header>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  height: 64px;
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  
  &-icon {
    font-size: 24px;
  }
  
  &-text {
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
    color: var(--color-primary);
  }
}

.nav-links {
  display: flex;
  gap: 24px;
  flex: 1;
}

.nav-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  font-weight: 500;
  transition: color 150ms ease;
  
  &:hover,
  &.router-link-active {
    color: var(--color-primary);
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 150ms ease;
  
  &:hover {
    background: $neutral-100;
    color: var(--color-primary);
  }
}

.dashboard-btn-mobile {
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  transition: all 150ms ease;
  
  &:hover {
    background: $neutral-100;
    color: var(--color-primary);
  }
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .dashboard-btn-mobile {
    display: flex;
  }
}
</style>
