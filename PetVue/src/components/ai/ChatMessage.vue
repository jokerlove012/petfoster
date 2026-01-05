<template>
  <div class="chat-message" :class="[message.role, message.type]">
    <!-- 用户消息 -->
    <div v-if="message.role === 'user'" class="message-bubble user">
      <p>{{ message.content }}</p>
    </div>

    <!-- AI 消息 -->
    <div v-else class="ai-message">
      <div class="ai-avatar">
        <Bot :size="16" />
      </div>
      <div class="message-content">
        <div class="message-bubble assistant">
          <p v-html="formatContent(message.content)"></p>
        </div>

        <!-- 护理建议卡片 -->
        <div
          v-if="message.type === 'care_tip' && message.metadata?.careTip"
          class="care-tip-card"
        >
          <div class="tip-header">
            <span class="tip-icon">{{ message.metadata.careTip.icon || '💡' }}</span>
            <span class="tip-title">{{ message.metadata.careTip.title }}</span>
          </div>
          <div class="tip-content">
            <p v-html="formatContent(message.metadata.careTip.content)"></p>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div
          v-if="message.metadata?.actions?.length"
          class="message-actions"
        >
          <button
            v-for="action in message.metadata.actions"
            :key="action.id"
            class="action-btn"
            @click="$emit('action', action)"
          >
            {{ action.label }}
          </button>
        </div>

        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Bot } from 'lucide-vue-next'
import type { AIMessage, QuickAction } from '@/types/ai'

defineProps<{
  message: AIMessage
}>()

defineEmits<{
  (e: 'action', action: QuickAction): void
}>()

// 格式化内容（支持简单的 markdown）
function formatContent(content: string): string {
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

// 格式化时间
function formatTime(timestamp: string): string {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped lang="scss">
.chat-message {
  display: flex;

  &.user {
    justify-content: flex-end;
  }
}

.message-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;

  p {
    margin: 0;
  }

  &.user {
    background: var(--color-primary);
    color: white;
    border-bottom-right-radius: 4px;
  }

  &.assistant {
    background: var(--color-neutral-100);
    color: var(--color-text-primary);
    border-bottom-left-radius: 4px;
  }
}

.ai-message {
  display: flex;
  gap: 8px;
  max-width: 90%;
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

.message-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.care-tip-card {
  background: linear-gradient(
    135deg,
    var(--color-accent-light) 0%,
    var(--color-primary-light) 100%
  );
  border-radius: var(--radius-md);
  padding: 12px;
  margin-top: 4px;
}

.tip-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  .tip-icon {
    font-size: 20px;
  }

  .tip-title {
    font-weight: 600;
    color: var(--color-text-primary);
  }
}

.tip-content {
  p {
    margin: 0;
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.6;
  }
}

.message-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.action-btn {
  padding: 6px 12px;
  background: var(--color-surface);
  border: 1px solid var(--color-primary);
  border-radius: 16px;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--color-primary);
    color: white;
  }
}

.message-time {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-left: 4px;
}
</style>
