<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores'
import type { User, UserRole } from '@/types/user'

interface Props {
  user: User
}

const props = defineProps<Props>()

const router = useRouter()
const authStore = useAuthStore()
const isOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

// 角色显示名称映射
const roleLabels: Record<UserRole, string> = {
  pet_owner: '宠物主人',
  institution_staff: '寄养机构',
  admin: '管理员'
}

// 角色对应的颜色
const roleColors: Record<UserRole, string> = {
  pet_owner: 'var(--color-primary)',
  institution_staff: 'var(--color-accent)',
  admin: '#8B5CF6'
}

const roleLabel = computed(() => roleLabels[props.user.role] || '用户')
const roleColor = computed(() => roleColors[props.user.role] || 'var(--color-primary)')
const userInitial = computed(() => props.user.name?.charAt(0) || 'U')

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const closeDropdown = () => {
  isOpen.value = false
}

const handleLogout = async () => {
  closeDropdown()
  await authStore.logout()
  router.push('/login')
}

const navigateTo = (path: string) => {
  closeDropdown()
  router.push(path)
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (dropdownRef.value && !dropdownRef.value.contains(target)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>


<template>
  <div 
    ref="dropdownRef"
    class="user-dropdown"
  >
    <button 
      class="user-trigger" 
      @click="toggleDropdown"
      :class="{ active: isOpen }"
    >
      <div class="avatar" :style="{ backgroundColor: roleColor }">
        <img v-if="user.avatar" :src="user.avatar" :alt="user.name" />
        <span v-else>{{ userInitial }}</span>
      </div>
      <div class="user-info">
        <span class="user-name">{{ user.name }}</span>
        <span class="user-role" :style="{ color: roleColor }">{{ roleLabel }}</span>
      </div>
      <svg 
        class="chevron" 
        :class="{ rotated: isOpen }"
        width="16" 
        height="16" 
        viewBox="0 0 24 24" 
        fill="none" 
        stroke="currentColor" 
        stroke-width="2"
      >
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="dropdown-panel">
        <div class="dropdown-header">
          <div class="avatar-large" :style="{ backgroundColor: roleColor }">
            <img v-if="user.avatar" :src="user.avatar" :alt="user.name" />
            <span v-else>{{ userInitial }}</span>
          </div>
          <div class="header-info">
            <span class="header-name">{{ user.name }}</span>
            <span class="header-role" :style="{ color: roleColor }">
              <span class="role-dot" :style="{ backgroundColor: roleColor }"></span>
              {{ roleLabel }}
            </span>
          </div>
        </div>

        <div class="dropdown-divider"></div>

        <div class="dropdown-menu">
          <!-- 机构员工显示机构资料 -->
          <button v-if="user.role === 'institution_staff'" class="menu-item" @click="navigateTo('/institution/profile')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
            <span>机构资料</span>
          </button>
          <!-- 其他用户显示个人资料 -->
          <button v-else class="menu-item" @click="navigateTo('/user/profile')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>个人资料</span>
          </button>

          <button class="menu-item" @click="navigateTo('/user/orders')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
            </svg>
            <span>我的订单</span>
          </button>

          <button class="menu-item" @click="navigateTo('/user/favorites')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
            <span>我的收藏</span>
          </button>

          <button class="menu-item" @click="navigateTo('/settings')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"></circle>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
            </svg>
            <span>设置</span>
          </button>
        </div>

        <div class="dropdown-divider"></div>

        <div class="dropdown-footer">
          <button class="menu-item logout" @click="handleLogout">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            <span>退出登录</span>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>


<style lang="scss" scoped>
.user-dropdown {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 150ms ease;

  &:hover,
  &.active {
    background: var(--color-neutral-100);
  }

  .avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 600;
    font-size: 14px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .user-info {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }

  .user-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text-primary);
    line-height: 1.2;
  }

  .user-role {
    font-size: 12px;
    font-weight: 500;
    line-height: 1.2;
  }

  .chevron {
    color: var(--color-text-muted);
    transition: transform 200ms ease;

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

.dropdown-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 240px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--color-border);
  overflow: hidden;
  z-index: 1000;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-neutral-50);

  .avatar-large {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 700;
    font-size: 18px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .header-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .header-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-primary);
  }

  .header-role {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 500;
  }

  .role-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border);
}

.dropdown-menu,
.dropdown-footer {
  padding: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px 12px;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 150ms ease;
  color: var(--color-text-primary);
  font-size: 14px;
  text-align: left;

  svg {
    color: var(--color-text-muted);
    flex-shrink: 0;
  }

  &:hover {
    background: var(--color-neutral-100);

    svg {
      color: var(--color-primary);
    }
  }

  &.logout {
    color: var(--color-error);

    svg {
      color: var(--color-error);
    }

    &:hover {
      background: rgba(239, 68, 68, 0.1);
    }
  }
}

// 下拉动画
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 200ms ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 响应式隐藏用户信息
@media (max-width: 768px) {
  .user-trigger {
    .user-info {
      display: none;
    }

    .chevron {
      display: none;
    }
  }

  .dropdown-panel {
    right: -8px;
  }
}
</style>
