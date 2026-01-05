<template>
  <Transition name="slide-up">
    <div v-if="aiStore.isOpen" class="ai-chat-window">
      <div class="chat-header">
        <div class="header-left">
          <div class="ai-avatar">
            <Bot :size="20" />
          </div>
          <div class="header-info">
            <h3>AI 助手</h3>
            <span class="status">在线</span>
          </div>
        </div>
        <div class="header-actions">
          <button class="action-btn" @click="handleNewChat" title="新对话">
            <Plus :size="18" />
          </button>
          <button class="action-btn" @click="handleHistory" title="历史记录">
            <History :size="18" />
          </button>
          <button class="action-btn close" @click="aiStore.setOpen(false)">
            <X :size="18" />
          </button>
        </div>
      </div>

      <div class="chat-body" ref="chatBodyRef">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-icon">🐾</div>
          <h4>您好！我是宠物寄养助手</h4>
          <p>有什么可以帮您的吗？</p>
          
          <SuggestedQuestions
            :questions="aiStore.suggestedQuestions"
            @select="handleQuestionSelect"
          />
        </div>

        <!-- 消息列表 -->
        <div v-else class="messages-list">
          <ChatMessage
            v-for="msg in messages"
            :key="msg.id"
            :message="msg"
            @action="handleAction"
          />
          
          <!-- 打字指示器 -->
          <TypingIndicator v-if="aiStore.isTyping" />
        </div>
      </div>

      <!-- 推荐问题（有消息时显示在底部） -->
      <div v-if="messages.length > 0 && currentSuggestions.length > 0" class="suggestions-bar">
        <button
          v-for="q in currentSuggestions.slice(0, 2)"
          :key="q.id"
          class="suggestion-chip"
          @click="handleQuestionSelect(q)"
        >
          {{ q.text }}
        </button>
      </div>

      <div class="chat-footer">
        <div class="input-wrapper">
          <input
            v-model="inputMessage"
            type="text"
            placeholder="输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="aiStore.isTyping"
          />
          <button 
            class="send-btn" 
            @click="sendMessage"
            :disabled="!inputMessage.trim() || aiStore.isTyping"
          >
            <Send :size="18" />
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Bot, X, Plus, History, Send } from 'lucide-vue-next'
import { useAIStore } from '@/stores/ai'
import { aiApi, type ChatMessage as ApiChatMessage } from '@/api/ai'
import ChatMessage from './ChatMessage.vue'
import TypingIndicator from './TypingIndicator.vue'
import SuggestedQuestions from './SuggestedQuestions.vue'
import type { SuggestedQuestion, QuickAction } from '@/types/ai'

const router = useRouter()
const aiStore = useAIStore()

const inputMessage = ref('')
const chatBodyRef = ref<HTMLElement | null>(null)
const currentSuggestions = ref<SuggestedQuestion[]>([])

const messages = computed(() => aiStore.currentMessages)

// 获取初始推荐问题
const getInitialSuggestedQuestions = (): SuggestedQuestion[] => [
  { id: '1', text: '如何预约寄养服务？', category: 'booking' },
  { id: '2', text: '寄养期间宠物的照顾标准是什么？', category: 'service' },
  { id: '3', text: '如何查看我的订单？', category: 'order' }
]

// 构建历史消息用于API调用
const buildHistory = (): ApiChatMessage[] => {
  return messages.value.map(msg => ({
    role: msg.role as 'user' | 'assistant',
    content: msg.content
  }))
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动
watch(messages, () => {
  scrollToBottom()
})

// 发送消息
const sendMessage = async () => {
  const text = inputMessage.value.trim()
  if (!text || aiStore.isTyping) return

  // 添加用户消息
  aiStore.addMessage({
    role: 'user',
    type: 'text',
    content: text
  })

  inputMessage.value = ''
  scrollToBottom()

  // 显示打字指示器
  aiStore.setTyping(true)

  try {
    // 调用真实的 AI API
    const history = buildHistory().slice(0, -1) // 不包含刚添加的消息
    const res = await aiApi.chat(text, history) as any
    
    let content = '抱歉，我暂时无法回答这个问题，请稍后再试~'
    // API响应已被拦截器解包，res 就是 { code, message, data }
    if (res.data?.content) {
      content = res.data.content
    }

    // 添加AI消息
    aiStore.addMessage({
      role: 'assistant',
      type: 'text',
      content: content
    })

    // 更新推荐问题
    currentSuggestions.value = getInitialSuggestedQuestions()
  } catch (error) {
    console.error('AI请求失败:', error)
    aiStore.addMessage({
      role: 'assistant',
      type: 'text',
      content: '网络连接出现问题，请检查网络后重试~'
    })
  } finally {
    aiStore.setTyping(false)
    scrollToBottom()
  }
}

// 选择推荐问题
const handleQuestionSelect = (question: SuggestedQuestion) => {
  inputMessage.value = question.text
  sendMessage()
}

// 处理快捷操作
const handleAction = (action: QuickAction) => {
  if (action.action === 'navigate' && action.params?.path) {
    router.push(action.params.path as string)
    aiStore.setOpen(false)
  }
}

// 新对话
const handleNewChat = () => {
  aiStore.createConversation()
  currentSuggestions.value = getInitialSuggestedQuestions()
}

// 查看历史
const handleHistory = () => {
  router.push('/ai/history')
  aiStore.setOpen(false)
}

// 初始化
aiStore.init()
currentSuggestions.value = getInitialSuggestedQuestions()
</script>

<style scoped lang="scss">
.ai-chat-window {
  position: fixed;
  bottom: 100px;
  right: 24px;
  width: 380px;
  height: 560px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;

  @media (max-width: 640px) {
    bottom: 0;
    right: 0;
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #ff8f5c 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-info {
  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }

  .status {
    font-size: 12px;
    opacity: 0.9;
  }
}

.header-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }

  &.close:hover {
    background: rgba(255, 0, 0, 0.3);
  }
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.welcome-section {
  text-align: center;
  padding: 24px 16px;

  .welcome-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  h4 {
    margin: 0 0 8px;
    font-size: 18px;
    color: var(--color-text-primary);
  }

  p {
    margin: 0 0 24px;
    color: var(--color-text-secondary);
  }
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.suggestions-bar {
  display: flex;
  gap: 8px;
  padding: 8px 16px;
  border-top: 1px solid var(--color-border);
  overflow-x: auto;

  &::-webkit-scrollbar {
    display: none;
  }
}

.suggestion-chip {
  flex-shrink: 0;
  padding: 6px 12px;
  background: var(--color-primary-light);
  border: none;
  border-radius: 16px;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;

  &:hover {
    background: var(--color-primary);
    color: white;
  }
}

.chat-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);
}

.input-wrapper {
  display: flex;
  gap: 8px;
  background: var(--color-neutral-100);
  border-radius: var(--radius-lg);
  padding: 4px;

  input {
    flex: 1;
    padding: 10px 12px;
    border: none;
    background: transparent;
    font-size: 14px;
    outline: none;

    &::placeholder {
      color: var(--color-text-muted);
    }

    &:disabled {
      opacity: 0.6;
    }
  }
}

.send-btn {
  width: 40px;
  height: 40px;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-md);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: var(--color-primary-dark);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
