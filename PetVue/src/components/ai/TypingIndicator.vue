<template>
  <div class="typing-indicator">
    <div class="ai-avatar">
      <Bot :size="16" />
    </div>
    <div class="typing-bubble">
      <span class="dot"></span>
      <span class="dot"></span>
      <span class="dot"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Bot } from 'lucide-vue-next'
</script>

<style scoped lang="scss">
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-avatar {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #ff8f5c 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  background: var(--color-neutral-100);
  border-radius: 16px;
  border-bottom-left-radius: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  background: var(--color-text-muted);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;

  &:nth-child(1) {
    animation-delay: -0.32s;
  }

  &:nth-child(2) {
    animation-delay: -0.16s;
  }

  &:nth-child(3) {
    animation-delay: 0s;
  }
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
