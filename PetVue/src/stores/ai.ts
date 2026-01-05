import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  AIConversation,
  AIConversationSummary,
  AIMessage,
  SuggestedQuestion
} from '@/types/ai'

const CONVERSATIONS_KEY = 'pet_foster_ai_conversations'
const MAX_CONVERSATIONS = 50

export const useAIStore = defineStore('ai', () => {
  // State
  const isOpen = ref(false)
  const isTyping = ref(false)
  const currentConversation = ref<AIConversation | null>(null)
  const conversations = ref<AIConversation[]>([])
  const suggestedQuestions = ref<SuggestedQuestion[]>([
    { id: '1', text: '附近有哪些评分高的寄养机构？', category: 'institution' },
    { id: '2', text: '如何选择适合我宠物的寄养服务？', category: 'pet_care' },
    { id: '3', text: '预约流程是怎样的？', category: 'booking' },
    { id: '4', text: '支持哪些支付方式？', category: 'payment' }
  ])

  // Getters
  const conversationSummaries = computed<AIConversationSummary[]>(() => {
    return conversations.value
      .map((conv) => ({
        id: conv.id,
        title: conv.title,
        lastMessage:
          conv.messages.length > 0
            ? conv.messages[conv.messages.length - 1].content.slice(0, 50)
            : '',
        messageCount: conv.messages.length,
        createdAt: conv.createdAt,
        updatedAt: conv.updatedAt
      }))
      .sort(
        (a, b) =>
          new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      )
  })

  const hasConversations = computed(() => conversations.value.length > 0)

  const currentMessages = computed(() => {
    return currentConversation.value?.messages || []
  })

  // Actions
  function toggleOpen() {
    isOpen.value = !isOpen.value
  }

  function setOpen(value: boolean) {
    isOpen.value = value
  }

  function setTyping(value: boolean) {
    isTyping.value = value
  }

  function generateId(): string {
    return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
  }

  function createConversation(title?: string): AIConversation {
    const now = new Date().toISOString()
    const conversation: AIConversation = {
      id: generateId(),
      title: title || '新对话',
      messages: [],
      createdAt: now,
      updatedAt: now
    }

    conversations.value.unshift(conversation)
    currentConversation.value = conversation

    // 限制会话数量
    if (conversations.value.length > MAX_CONVERSATIONS) {
      conversations.value = conversations.value.slice(0, MAX_CONVERSATIONS)
    }

    saveToStorage()
    return conversation
  }

  function selectConversation(id: string) {
    const conversation = conversations.value.find((c) => c.id === id)
    if (conversation) {
      currentConversation.value = conversation
    }
  }

  function deleteConversation(id: string) {
    const index = conversations.value.findIndex((c) => c.id === id)
    if (index > -1) {
      conversations.value.splice(index, 1)
      if (currentConversation.value?.id === id) {
        currentConversation.value =
          conversations.value.length > 0 ? conversations.value[0] : null
      }
      saveToStorage()
    }
  }

  function addMessage(message: Omit<AIMessage, 'id' | 'timestamp'>): AIMessage {
    if (!currentConversation.value) {
      createConversation()
    }

    const newMessage: AIMessage = {
      ...message,
      id: generateId(),
      timestamp: new Date().toISOString()
    }

    currentConversation.value!.messages.push(newMessage)
    currentConversation.value!.updatedAt = newMessage.timestamp

    // 更新会话标题（使用第一条用户消息）
    if (
      message.role === 'user' &&
      currentConversation.value!.messages.filter((m) => m.role === 'user')
        .length === 1
    ) {
      currentConversation.value!.title = message.content.slice(0, 30)
    }

    saveToStorage()
    return newMessage
  }

  function clearCurrentConversation() {
    if (currentConversation.value) {
      currentConversation.value.messages = []
      currentConversation.value.updatedAt = new Date().toISOString()
      saveToStorage()
    }
  }

  function setSuggestedQuestions(questions: SuggestedQuestion[]) {
    suggestedQuestions.value = questions
  }

  // 搜索会话
  function searchConversations(query: string): AIConversationSummary[] {
    if (!query.trim()) {
      return conversationSummaries.value
    }

    const lowerQuery = query.toLowerCase()
    return conversationSummaries.value.filter(
      (conv) =>
        conv.title.toLowerCase().includes(lowerQuery) ||
        conv.lastMessage.toLowerCase().includes(lowerQuery)
    )
  }

  // 持久化
  function saveToStorage() {
    try {
      localStorage.setItem(CONVERSATIONS_KEY, JSON.stringify(conversations.value))
    } catch (e) {
      console.error('Failed to save AI conversations:', e)
    }
  }

  function loadFromStorage() {
    try {
      const stored = localStorage.getItem(CONVERSATIONS_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        if (Array.isArray(parsed)) {
          conversations.value = parsed
          if (conversations.value.length > 0) {
            currentConversation.value = conversations.value[0]
          }
        }
      }
    } catch (e) {
      console.error('Failed to load AI conversations:', e)
      conversations.value = []
    }
  }

  function init() {
    loadFromStorage()
  }

  return {
    // State
    isOpen,
    isTyping,
    currentConversation,
    conversations,
    suggestedQuestions,
    // Getters
    conversationSummaries,
    hasConversations,
    currentMessages,
    // Actions
    toggleOpen,
    setOpen,
    setTyping,
    createConversation,
    selectConversation,
    deleteConversation,
    addMessage,
    clearCurrentConversation,
    setSuggestedQuestions,
    searchConversations,
    init
  }
})
