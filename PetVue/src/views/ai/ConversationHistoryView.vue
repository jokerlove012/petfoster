<template>
  <div class="conversation-history-view">
    <div class="page-header">
      <h1>对话历史</h1>
      <p>查看和管理您与 AI 助手的对话记录</p>
    </div>

    <div class="search-bar">
      <Search :size="18" />
      <input
        v-model="searchQuery"
        type="text"
        placeholder="搜索对话..."
      />
    </div>

    <EmptyState
      v-if="filteredConversations.length === 0"
      icon="message"
      title="暂无对话记录"
      :description="searchQuery ? '没有找到匹配的对话' : '开始与 AI 助手对话吧'"
      actionText="开始对话"
      @action="startNewChat"
    />

    <div v-else class="conversations-list">
      <div
        v-for="conv in filteredConversations"
        :key="conv.id"
        class="conversation-item"
        @click="openConversation(conv.id)"
      >
        <div class="conv-icon">
          <MessageSquare :size="20" />
        </div>
        <div class="conv-content">
          <h4>{{ conv.title }}</h4>
          <p class="last-message">{{ conv.lastMessage || '暂无消息' }}</p>
          <div class="conv-meta">
            <span class="message-count">
              <MessageCircle :size="12" />
              {{ conv.messageCount }} 条消息
            </span>
            <span class="time">{{ formatTime(conv.updatedAt) }}</span>
          </div>
        </div>
        <button
          class="delete-btn"
          @click.stop="deleteConversation(conv.id)"
          title="删除对话"
        >
          <Trash2 :size="16" />
        </button>
      </div>
    </div>

    <div v-if="filteredConversations.length > 0" class="list-footer">
      <p>共 {{ filteredConversations.length }} 个对话</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, MessageSquare, MessageCircle, Trash2 } from 'lucide-vue-next'
import { useAIStore } from '@/stores/ai'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
const aiStore = useAIStore()

const searchQuery = ref('')

const filteredConversations = computed(() => {
  return aiStore.searchConversations(searchQuery.value)
})

function formatTime(timestamp: string): string {
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays === 0) {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric'
    })
  }
}

function openConversation(id: string) {
  aiStore.selectConversation(id)
  aiStore.setOpen(true)
  router.push('/')
}

function deleteConversation(id: string) {
  if (confirm('确定要删除这个对话吗？')) {
    aiStore.deleteConversation(id)
  }
}

function startNewChat() {
  aiStore.createConversation()
  aiStore.setOpen(true)
  router.push('/')
}

onMounted(() => {
  aiStore.init()
})
</script>

<style scoped lang="scss">
.conversation-history-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 8px;
    color: var(--color-text-primary);
  }

  p {
    margin: 0;
    color: var(--color-text-secondary);
  }
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  margin-bottom: 24px;

  svg {
    color: var(--color-text-muted);
  }

  input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 14px;
    outline: none;

    &::placeholder {
      color: var(--color-text-muted);
    }
  }
}

.conversations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-sm);

    .delete-btn {
      opacity: 1;
    }
  }
}

.conv-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  background: linear-gradient(
    135deg,
    var(--color-primary-light) 0%,
    var(--color-accent-light) 100%
  );
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.conv-content {
  flex: 1;
  min-width: 0;

  h4 {
    margin: 0 0 4px;
    font-size: 15px;
    font-weight: 600;
    color: var(--color-text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .last-message {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--color-text-secondary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.conv-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-muted);

  .message-count {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.delete-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--color-text-muted);
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;

  &:hover {
    background: var(--color-error);
    color: white;
  }
}

.list-footer {
  text-align: center;
  padding: 16px;
  color: var(--color-text-muted);
  font-size: 13px;

  p {
    margin: 0;
  }
}

@media (max-width: 640px) {
  .conversation-history-view {
    padding: 16px;
  }

  .delete-btn {
    opacity: 1;
  }
}
</style>
