<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useInstitutionStore, useAuthStore } from '@/stores'

interface Props {
  institutionId: string
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md'
})

const router = useRouter()
const institutionStore = useInstitutionStore()
const authStore = useAuthStore()

const isFavorite = computed(() => institutionStore.favorites.includes(props.institutionId))
const isAuthenticated = computed(() => authStore.isAuthenticated)

const iconSize = computed(() => {
  switch (props.size) {
    case 'sm': return 16
    case 'lg': return 28
    default: return 20
  }
})

const handleClick = (e: Event) => {
  e.preventDefault()
  e.stopPropagation()
  
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录')
    router.push({ name: 'login' })
    return
  }
  
  institutionStore.toggleFavorite(props.institutionId)
}
</script>

<template>
  <button 
    :class="['favorite-button', `favorite-button--${size}`, { active: isFavorite }]"
    @click="handleClick"
    :title="isFavorite ? '取消收藏' : '添加收藏'"
  >
    <svg 
      :width="iconSize" 
      :height="iconSize" 
      viewBox="0 0 24 24" 
      :fill="isFavorite ? 'currentColor' : 'none'" 
      stroke="currentColor" 
      stroke-width="2"
    >
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
    </svg>
  </button>
</template>

<style lang="scss" scoped>
.favorite-button {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 200ms ease;
  
  &:hover {
    background: white;
    transform: scale(1.1);
    color: var(--color-error);
  }
  
  &.active {
    color: var(--color-error);
  }
  
  &--sm {
    width: 28px;
    height: 28px;
  }
  
  &--md {
    width: 36px;
    height: 36px;
  }
  
  &--lg {
    width: 48px;
    height: 48px;
  }
}
</style>
