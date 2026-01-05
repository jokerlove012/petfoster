<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { MessageCircle, Search, Send, Phone, MoreVertical, Image, Paperclip } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'

const searchKeyword = ref('')
const messageInput = ref('')
const selectedConversation = ref<string | null>('1')

// 会话列表
const conversations = ref([
  { id: '1', user: '张三', avatar: '🧑', lastMessage: '好的，谢谢您的回复', time: '10:30', unread: 2, orderId: 'ORD20250115001' },
  { id: '2', user: '李四', avatar: '👩', lastMessage: '请问我的宠物今天状态怎么样？', time: '09:15', unread: 1, orderId: 'ORD20250114002' },
  { id: '3', user: '王五', avatar: '🧔', lastMessage: '收到，我明天来接', time: '昨天', unread: 0, orderId: 'ORD20250113003' },
  { id: '4', user: '赵六', avatar: '👱', lastMessage: '可以延长寄养时间吗？', time: '昨天', unread: 0, orderId: 'ORD20250112004' }
])

// 消息记录
const messages = ref<Record<string, Array<{ id: string; content: string; time: string; isSelf: boolean }>>>({
  '1': [
    { id: 'm1', content: '您好，请问我家狗狗今天吃饭情况怎么样？', time: '10:00', isSelf: false },
    { id: 'm2', content: '您好，狗狗今天食欲很好，已经吃完了早餐，精神状态也很棒！', time: '10:15', isSelf: true },
    { id: 'm3', content: '太好了，能发一张照片给我看看吗？', time: '10:20', isSelf: false },
    { id: 'm4', content: '[图片] 狗狗正在玩耍呢~', time: '10:25', isSelf: true },
    { id: 'm5', content: '好的，谢谢您的回复', time: '10:30', isSelf: false }
  ],
  '2': [
    { id: 'm1', content: '请问我的宠物今天状态怎么样？', time: '09:15', isSelf: false }
  ]
})

const currentConversation = computed(() => conversations.value.find(c => c.id === selectedConversation.value))
const currentMessages = computed(() => messages.value[selectedConversation.value || ''] || [])

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  return conversations.value.filter(c => c.user.includes(searchKeyword.value) || c.orderId.includes(searchKeyword.value))
})

const selectConversation = (id: string) => {
  selectedConversation.value = id
  const conv = conversations.value.find(c => c.id === id)
  if (conv) conv.unread = 0
}

const sendMessage = () => {
  if (!messageInput.value.trim() || !selectedConversation.value) return
  const newMsg = { id: `m${Date.now()}`, content: messageInput.value, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }), isSelf: true }
  if (!messages.value[selectedConversation.value]) messages.value[selectedConversation.value] = []
  messages.value[selectedConversation.value].push(newMsg)
  const conv = conversations.value.find(c => c.id === selectedConversation.value)
  if (conv) { conv.lastMessage = messageInput.value; conv.time = newMsg.time }
  messageInput.value = ''
  ElMessage.success('消息已发送')
}
</script>

<template>
  <div class="message-view">
    <div class="page-header">
      <h1>💬 消息中心</h1>
      <p>与客户沟通交流</p>
    </div>

    <div class="chat-container">
      <!-- 会话列表 -->
      <div class="conversation-list">
        <div class="search-box">
          <Search :size="18" />
          <input v-model="searchKeyword" placeholder="搜索用户/订单号" />
        </div>
        <div class="conversations">
          <div v-for="conv in filteredConversations" :key="conv.id" class="conversation-item"
            :class="{ active: selectedConversation === conv.id }" @click="selectConversation(conv.id)">
            <div class="avatar">{{ conv.avatar }}</div>
            <div class="conv-info">
              <div class="conv-header">
                <span class="user-name">{{ conv.user }}</span>
                <span class="time">{{ conv.time }}</span>
              </div>
              <div class="last-message">{{ conv.lastMessage }}</div>
            </div>
            <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread }}</span>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-area">
        <template v-if="currentConversation">
          <div class="chat-header">
            <span class="chat-user">{{ currentConversation.user }}</span>
            <span class="order-id">订单: {{ currentConversation.orderId }}</span>
          </div>
          <div class="messages-container">
            <div v-for="msg in currentMessages" :key="msg.id" class="message" :class="{ self: msg.isSelf }">
              <div class="message-bubble">{{ msg.content }}</div>
              <span class="message-time">{{ msg.time }}</span>
            </div>
          </div>
          <div class="chat-input">
            <input v-model="messageInput" placeholder="输入消息..." @keyup.enter="sendMessage" />
            <button class="btn-send" @click="sendMessage"><Send :size="18" /></button>
          </div>
        </template>
        <div v-else class="empty-chat">
          <MessageCircle :size="48" />
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.message-view { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header { margin-bottom: 24px;
  h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
  p { color: #6B6560; margin: 0; }
}

.chat-container {
  display: grid; grid-template-columns: 320px 1fr; gap: 20px; height: calc(100vh - 200px); min-height: 500px;
  @media (max-width: 768px) { grid-template-columns: 1fr; }
}

.conversation-list {
  background: white; border-radius: 14px; display: flex; flex-direction: column; overflow: hidden;
  .search-box { display: flex; align-items: center; gap: 10px; padding: 16px; border-bottom: 1px solid #F0EFED;
    input { border: none; outline: none; flex: 1; font-size: 14px; } color: #9A958F;
  }
  .conversations { flex: 1; overflow-y: auto; }
}

.conversation-item {
  display: flex; align-items: center; gap: 12px; padding: 14px 16px; cursor: pointer; transition: background 0.2s;
  &:hover { background: #F8F8F7; }
  &.active { background: #F3EEFF; }
  .avatar { width: 44px; height: 44px; background: #F0EFED; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; }
  .conv-info { flex: 1; min-width: 0;
    .conv-header { display: flex; justify-content: space-between; margin-bottom: 4px;
      .user-name { font-weight: 600; font-size: 14px; }
      .time { font-size: 12px; color: #9A958F; }
    }
    .last-message { font-size: 13px; color: #6B6560; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  }
  .unread-badge { background: #ff4d4f; color: white; font-size: 11px; padding: 2px 6px; border-radius: 10px; }
}

.chat-area {
  background: white; border-radius: 14px; display: flex; flex-direction: column; overflow: hidden;
  .chat-header { padding: 16px 20px; border-bottom: 1px solid #F0EFED; display: flex; justify-content: space-between; align-items: center;
    .chat-user { font-weight: 600; font-size: 16px; }
    .order-id { font-size: 13px; color: #9A958F; }
  }
  .messages-container { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 16px; }
  .chat-input { display: flex; gap: 12px; padding: 16px 20px; border-top: 1px solid #F0EFED;
    input { flex: 1; padding: 12px 16px; border: 1px solid #E8E6E3; border-radius: 10px; font-size: 14px; outline: none;
      &:focus { border-color: #722ed1; }
    }
    .btn-send { width: 44px; height: 44px; background: #722ed1; color: white; border: none; border-radius: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
  }
}

.message {
  display: flex; flex-direction: column; max-width: 70%;
  &.self { align-self: flex-end; align-items: flex-end; }
  .message-bubble { padding: 12px 16px; border-radius: 16px; font-size: 14px; line-height: 1.5; background: #F8F8F7; color: #2D2A26; }
  &.self .message-bubble { background: #722ed1; color: white; }
  .message-time { font-size: 11px; color: #9A958F; margin-top: 4px; }
}

.empty-chat { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #9A958F;
  p { margin-top: 16px; }
}
</style>
