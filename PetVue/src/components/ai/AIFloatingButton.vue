<template>
  <button
    class="ai-floating-button"
    :class="{ active: aiStore.isOpen, 'has-unread': hasUnread }"
    @click="handleClick"
  >
    <Transition name="icon-switch" mode="out-in">
      <X v-if="aiStore.isOpen" :size="24" key="close" />
      <Bot v-else :size="24" key="bot" />
    </Transition>
    <span v-if="hasUnread && !aiStore.isOpen" class="unread-badge"></span>
    <span class="tooltip">AI 助手</span>
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Bot, X } from 'lucide-vue-next'
import { useAIStore } from '@/stores/ai'

const aiStore = useAIStore()
const hasUnread = ref(false)

function handleClick() {
  aiStore.toggleOpen()
  if (aiStore.isOpen) {
    hasUnread.value = false
  }
}
</script>

<style scoped lang="scss">
.ai-floating-button {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #ff8f5c 100%);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 999;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 28px rgba(255, 107, 53, 0.5);

    .tooltip {
      opacity: 1;
      transform: translateX(-100%) translateX(-12px);
    }
  }

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background: var(--color-neutral-700);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  }

  &.has-unread::after {
    content: '';
    position: absolute;
    top: -4px;
    right: -4px;
    width: 16px;
    height: 16px;
    background: var(--color-error);
    border: 2px solid white;
    border-radius: 50%;
    animation: pulse 2s infinite;
  }

  @media (max-width: 640px) {
    bottom: 80px;
    right: 16px;
    width: 52px;
    height: 52px;
  }
}

.unread-badge {
  position: absolute;
  top: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: var(--color-error);
  border: 2px solid white;
  border-radius: 50%;
}

.tooltip {
  position: absolute;
  right: 100%;
  top: 50%;
  transform: translateY(-50%) translateX(-8px);
  padding: 6px 12px;
  background: var(--color-neutral-800);
  color: white;
  font-size: 12px;
  border-radius: var(--radius-sm);
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: all 0.2s;

  &::after {
    content: '';
    position: absolute;
    right: -4px;
    top: 50%;
    transform: translateY(-50%);
    border: 4px solid transparent;
    border-left-color: var(--color-neutral-800);
  }
}

// 图标切换动画
.icon-switch-enter-active,
.icon-switch-leave-active {
  transition: all 0.2s ease;
}

.icon-switch-enter-from {
  opacity: 0;
  transform: rotate(-90deg) scale(0.5);
}

.icon-switch-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(239, 68, 68, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0);
  }
}
</style>
