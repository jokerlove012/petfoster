<template>
  <div :class="['empty-state', `empty-state--${size}`]">
    <div class="empty-visual">
      <span v-if="icon" class="empty-icon">{{ icon }}</span>
      <img v-else-if="image" :src="image" :alt="title" class="empty-image" />
      <span v-else class="empty-icon">{{ defaultIcon }}</span>
    </div>
    
    <h3 v-if="title" class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-description">{{ description }}</p>
    
    <div v-if="$slots.default || actionText" class="empty-actions">
      <slot>
        <button 
          v-if="actionText" 
          class="empty-action-btn"
          @click="$emit('action')"
        >
          {{ actionText }}
        </button>
      </slot>
    </div>
    
    <div v-if="suggestions?.length" class="empty-suggestions">
      <p class="suggestions-title">您可以尝试：</p>
      <ul>
        <li v-for="(suggestion, index) in suggestions" :key="index">
          {{ suggestion }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type EmptyType = 'default' | 'search' | 'data' | 'error' | 'network' | 'permission' | 'favorite' | 'order' | 'notification'

interface Props {
  type?: EmptyType
  title?: string
  description?: string
  icon?: string
  image?: string
  actionText?: string
  suggestions?: string[]
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'md'
})

defineEmits<{
  (e: 'action'): void
}>()

const presets: Record<EmptyType, { icon: string; title: string; description: string }> = {
  default: {
    icon: '📭',
    title: '暂无内容',
    description: '这里还没有任何内容'
  },
  search: {
    icon: '🔍',
    title: '未找到结果',
    description: '换个关键词试试吧'
  },
  data: {
    icon: '📊',
    title: '暂无数据',
    description: '数据正在路上，请稍后再来'
  },
  error: {
    icon: '😵',
    title: '出错了',
    description: '加载失败，请稍后重试'
  },
  network: {
    icon: '📡',
    title: '网络异常',
    description: '请检查网络连接后重试'
  },
  permission: {
    icon: '🔒',
    title: '无权访问',
    description: '您没有权限查看此内容'
  },
  favorite: {
    icon: '💝',
    title: '暂无收藏',
    description: '快去发现喜欢的机构吧'
  },
  order: {
    icon: '📋',
    title: '暂无订单',
    description: '您还没有任何预约订单'
  },
  notification: {
    icon: '🔔',
    title: '暂无通知',
    description: '您的消息都已处理完毕'
  }
}

const preset = computed(() => presets[props.type])
const defaultIcon = computed(() => props.icon || preset.value.icon)
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 24px;
  
  &--sm {
    padding: 24px 16px;
    
    .empty-icon {
      font-size: 48px;
    }
    
    .empty-image {
      width: 80px;
      height: 80px;
    }
    
    .empty-title {
      font-size: 15px;
    }
    
    .empty-description {
      font-size: 13px;
    }
  }
  
  &--md {
    .empty-icon {
      font-size: 72px;
    }
    
    .empty-image {
      width: 120px;
      height: 120px;
    }
    
    .empty-title {
      font-size: 18px;
    }
    
    .empty-description {
      font-size: 14px;
    }
  }
  
  &--lg {
    padding: 60px 32px;
    
    .empty-icon {
      font-size: 96px;
    }
    
    .empty-image {
      width: 160px;
      height: 160px;
    }
    
    .empty-title {
      font-size: 22px;
    }
    
    .empty-description {
      font-size: 16px;
    }
  }
}

.empty-visual {
  margin-bottom: 20px;
}

.empty-icon {
  display: block;
  opacity: 0.8;
}

.empty-image {
  object-fit: contain;
  opacity: 0.8;
}

.empty-title {
  font-weight: 600;
  color: var(--color-text-primary, #1a1a1a);
  margin: 0 0 8px;
}

.empty-description {
  color: var(--color-text-secondary, #666);
  margin: 0;
  max-width: 300px;
  line-height: 1.5;
}

.empty-actions {
  margin-top: 24px;
}

.empty-action-btn {
  padding: 12px 28px;
  background: var(--color-primary, #ff6b35);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: #e55a2b;
    transform: translateY(-2px);
  }
}

.empty-suggestions {
  margin-top: 24px;
  padding: 16px 20px;
  background: var(--color-surface, #f9f9f9);
  border-radius: 10px;
  text-align: left;
  
  .suggestions-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--color-text-secondary, #666);
    margin: 0 0 8px;
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
    
    li {
      font-size: 13px;
      color: var(--color-text-tertiary, #999);
      line-height: 1.6;
    }
  }
}
</style>
