<template>
  <div class="ai-chat-page">
    <div class="chat-container">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-info">
          <div class="ai-avatar">
            <Bot class="avatar-icon" />
          </div>
          <div class="header-text">
            <h1>AI 宠物助手</h1>
            <p>有任何关于宠物寄养的问题，都可以问我哦~</p>
          </div>
        </div>
        <router-link to="/ai/history" class="history-btn">
          <History class="btn-icon" />
          历史记录
        </router-link>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef">
        <!-- 欢迎消息 -->
        <div class="message ai-message" v-if="messages.length === 0">
          <div class="message-avatar">
            <Bot class="avatar-icon" />
          </div>
          <div class="message-content">
            <p>你好！我是宠物寄养AI助手 🐾</p>
            <p>我可以帮你：</p>
            <ul>
              <li>推荐合适的寄养机构</li>
              <li>解答宠物护理问题</li>
              <li>提供寄养注意事项</li>
              <li>帮你比较不同机构的服务</li>
            </ul>
            <p>请问有什么可以帮助你的？</p>
          </div>
        </div>

        <!-- 对话消息 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message"
          :class="msg.role === 'user' ? 'user-message' : 'ai-message'"
        >
          <div class="message-avatar">
            <User v-if="msg.role === 'user'" class="avatar-icon" />
            <Bot v-else class="avatar-icon" />
          </div>
          <div class="message-content">
            <p v-html="formatMessage(msg.content)"></p>
            <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="message ai-message">
          <div class="message-avatar">
            <Bot class="avatar-icon" />
          </div>
          <div class="message-content typing">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <!-- 快捷问题 -->
      <div class="quick-questions" v-if="messages.length === 0">
        <button
          v-for="q in quickQuestions"
          :key="q"
          class="quick-btn"
          @click="sendQuickQuestion(q)"
        >
          {{ q }}
        </button>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入你的问题..."
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button
          type="primary"
          :disabled="!inputText.trim() || loading"
          @click="sendMessage"
          circle
        >
          <Send class="send-icon" />
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Bot, User, Send, History } from 'lucide-vue-next'
import { aiApi, type ChatMessage } from '@/api/ai'

interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const messagesRef = ref<HTMLElement>()
const inputText = ref('')
const loading = ref(false)
const messages = ref<Message[]>([])

const quickQuestions = [
  '附近有哪些评价好的寄养机构？',
  '猫咪寄养需要注意什么？',
  '狗狗寄养一天大概多少钱？',
  '如何选择靠谱的寄养机构？'
]

// 格式化消息（支持换行和markdown加粗）
const formatMessage = (text: string) => {
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 发送快捷问题
const sendQuickQuestion = (question: string) => {
  inputText.value = question
  sendMessage()
}

// 构建历史消息
const buildHistory = (): ChatMessage[] => {
  return messages.value.map(msg => ({
    role: msg.role,
    content: msg.content
  }))
}

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: text,
    timestamp: new Date()
  })
  inputText.value = ''
  scrollToBottom()

  // 调用AI API
  loading.value = true
  try {
    const history = buildHistory().slice(0, -1) // 不包含刚添加的消息
    const res = await aiApi.chat(text, history)
    
    if (res.data?.content) {
      messages.value.push({
        role: 'assistant',
        content: res.data.content,
        timestamp: new Date()
      })
    } else {
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我暂时无法回答这个问题，请稍后再试~',
        timestamp: new Date()
      })
    }
  } catch (error) {
    console.error('AI请求失败:', error)
    messages.value.push({
      role: 'assistant',
      content: '网络连接出现问题，请检查网络后重试~',
      timestamp: new Date()
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped lang="scss">
.ai-chat-page {
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
  display: flex;
  justify-content: center;
}

.chat-container {
  width: 100%;
  max-width: 800px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-avatar {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar-icon {
    width: 28px;
    height: 28px;
  }
}

.header-text {
  h1 {
    margin: 0 0 4px;
    font-size: 20px;
    font-weight: 600;
  }

  p {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
  }
}

.history-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  color: #fff;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }

  .btn-icon {
    width: 16px;
    height: 16px;
  }
}

.chat-messages {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  min-height: 400px;
  max-height: 500px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.user-message {
    flex-direction: row-reverse;

    .message-content {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border-radius: 18px 18px 4px 18px;
    }

    .message-time {
      text-align: right;
      color: rgba(255, 255, 255, 0.7);
    }
  }

  &.ai-message {
    .message-content {
      background: #f5f7fa;
      border-radius: 18px 18px 18px 4px;
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8ecf1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .avatar-icon {
    width: 22px;
    height: 22px;
    color: #667eea;
  }
}

.user-message .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .avatar-icon {
    color: #fff;
  }
}

.message-content {
  max-width: 70%;
  padding: 14px 18px;

  p {
    margin: 0 0 8px;
    line-height: 1.6;

    &:last-child {
      margin-bottom: 0;
    }
  }

  ul {
    margin: 8px 0;
    padding-left: 20px;

    li {
      margin-bottom: 4px;
    }
  }

  &.typing {
    display: flex;
    gap: 4px;
    padding: 18px;

    .dot {
      width: 8px;
      height: 8px;
      background: #999;
      border-radius: 50%;
      animation: typing 1.4s infinite;

      &:nth-child(2) {
        animation-delay: 0.2s;
      }

      &:nth-child(3) {
        animation-delay: 0.4s;
      }
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.message-time {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 0 24px 20px;
}

.quick-btn {
  padding: 10px 16px;
  background: #f5f7fa;
  border: 1px solid #e8ecf1;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #667eea;
    border-color: #667eea;
    color: #fff;
  }
}

.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #eef2f7;
  background: #fafbfc;

  .el-input {
    flex: 1;
  }

  :deep(.el-textarea__inner) {
    border-radius: 20px;
    padding: 12px 18px;
    resize: none;
  }

  .el-button {
    width: 44px;
    height: 44px;
  }

  .send-icon {
    width: 20px;
    height: 20px;
  }
}

@media (max-width: 768px) {
  .ai-chat-page {
    padding: 0;
  }

  .chat-container {
    border-radius: 0;
    height: 100vh;
  }

  .chat-header {
    padding: 16px;
  }

  .header-text h1 {
    font-size: 18px;
  }

  .history-btn span {
    display: none;
  }

  .chat-messages {
    padding: 16px;
    max-height: none;
  }

  .message-content {
    max-width: 85%;
  }

  .quick-questions {
    padding: 0 16px 16px;
  }

  .chat-input {
    padding: 16px;
  }
}
</style>
