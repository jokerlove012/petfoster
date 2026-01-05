<script setup lang="ts">
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { Send, Image, Paperclip, Smile, MoreVertical } from 'lucide-vue-next'

interface Message {
  id: string
  senderId: string
  content: string
  type: 'text' | 'image' | 'file'
  fileUrl?: string
  fileName?: string
  createdAt: Date
  isRead: boolean
}

interface ChatParticipant {
  id: string
  name: string
  avatar?: string
  role: 'user' | 'institution'
}

const props = defineProps<{
  threadId: string
  currentUserId: string
  participant: ChatParticipant
}>()

const emit = defineEmits<{
  sendMessage: [content: string, type: 'text' | 'image' | 'file']
}>()

const messages = ref<Message[]>([])
const newMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

// 加载消息
const loadMessages = async () => {
  loading.value = true
  try {
    // TODO: 对接真实API
    // const res = await messageApi.getMessages(props.threadId)
    // messages.value = res.data
    
    // 暂时显示空消息列表
    messages.value = []
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim() || sending.value) return
  
  sending.value = true
  const content = newMessage.value.trim()
  newMessage.value = ''
  
  try {
    // 添加本地消息
    const message: Message = {
      id: Date.now().toString(),
      senderId: props.currentUserId,
      content,
      type: 'text',
      createdAt: new Date(),
      isRead: false
    }
    messages.value.push(message)
    
    await nextTick()
    scrollToBottom()
    
    emit('sendMessage', content, 'text')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期分隔
const formatDateSeparator = (date: Date) => {
  const today = new Date()
  const yesterday = new Date(today.getTime() - 86400000)
  
  if (date.toDateString() === today.toDateString()) {
    return '今天'
  }
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  return date.toLocaleDateString('zh-CN', {
    month: 'long',
    day: 'numeric'
  })
}

// 判断是否需要显示日期分隔
const shouldShowDateSeparator = (index: number) => {
  if (index === 0) return true
  
  const current = messages.value[index].createdAt
  const previous = messages.value[index - 1].createdAt
  
  return current.toDateString() !== previous.toDateString()
}

// 判断是否是自己发送的消息
const isOwnMessage = (message: Message) => {
  return message.senderId === props.currentUserId
}

// 处理键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

onMounted(() => {
  loadMessages()
})

watch(messages, () => {
  nextTick(() => scrollToBottom())
}, { deep: true })
</script>

<template>
  <div class="chat-thread">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="participant-info">
        <div class="participant-avatar">
          <img v-if="participant.avatar" :src="participant.avatar" :alt="participant.name" />
          <span v-else>{{ participant.name.charAt(0) }}</span>
        </div>
        <div class="participant-details">
          <h3>{{ participant.name }}</h3>
          <span class="participant-role">
            {{ participant.role === 'institution' ? '寄养机构' : '用户' }}
          </span>
        </div>
      </div>
      <button class="more-btn">
        <MoreVertical :size="20" />
      </button>
    </div>

    <!-- 消息列表 -->
    <div ref="messagesContainer" class="messages-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
      </div>

      <template v-else>
        <template v-for="(message, index) in messages" :key="message.id">
          <!-- 日期分隔 -->
          <div v-if="shouldShowDateSeparator(index)" class="date-separator">
            <span>{{ formatDateSeparator(message.createdAt) }}</span>
          </div>

          <!-- 消息气泡 -->
          <div 
            class="message-item"
            :class="{ own: isOwnMessage(message) }"
          >
            <div class="message-bubble">
              <p class="message-content">{{ message.content }}</p>
              <span class="message-time">{{ formatTime(message.createdAt) }}</span>
            </div>
          </div>
        </template>
      </template>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-actions">
        <button class="action-btn" title="发送图片">
          <Image :size="20" />
        </button>
        <button class="action-btn" title="发送文件">
          <Paperclip :size="20" />
        </button>
        <button class="action-btn" title="表情">
          <Smile :size="20" />
        </button>
      </div>
      <div class="input-wrapper">
        <textarea
          v-model="newMessage"
          class="message-input"
          placeholder="输入消息..."
          rows="1"
          @keydown="handleKeydown"
        ></textarea>
        <button 
          class="send-btn"
          :disabled="!newMessage.trim() || sending"
          @click="sendMessage"
        >
          <Send :size="20" />
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.chat-thread {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--color-background);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.participant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.participant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  span {
    color: white;
    font-size: 16px;
    font-weight: 600;
  }
}

.participant-details {
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 2px;
  }
  
  .participant-role {
    font-size: 12px;
    color: var(--color-text-secondary);
  }
}

.more-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background: var(--color-background);
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 24px;
  
  .loading-spinner {
    width: 32px;
    height: 32px;
    border: 3px solid var(--color-border);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.date-separator {
  display: flex;
  justify-content: center;
  padding: 8px 0;
  
  span {
    padding: 4px 12px;
    background: var(--color-surface);
    border-radius: 20px;
    font-size: 12px;
    color: var(--color-text-secondary);
  }
}

.message-item {
  display: flex;
  
  &.own {
    justify-content: flex-end;
    
    .message-bubble {
      background: var(--color-primary);
      color: white;
      border-radius: var(--radius-lg) var(--radius-lg) 4px var(--radius-lg);
      
      .message-time {
        color: rgba(255, 255, 255, 0.7);
      }
    }
  }
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg) var(--radius-lg) var(--radius-lg) 4px;
  box-shadow: var(--shadow-sm);
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 4px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: var(--color-text-tertiary);
  text-align: right;
}

.input-area {
  padding: 12px 20px;
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
}

.input-actions {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background: var(--color-background);
    color: var(--color-primary);
  }
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  max-height: 120px;
  
  &:focus {
    outline: none;
    border-color: var(--color-primary);
  }
  
  &::placeholder {
    color: var(--color-text-tertiary);
  }
}

.send-btn {
  width: 44px;
  height: 44px;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-lg);
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
</style>
