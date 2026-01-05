/**
 * AI 助手相关类型定义
 */

// 消息角色
export type MessageRole = 'user' | 'assistant' | 'system'

// 消息类型
export type MessageType = 'text' | 'recommendation' | 'care_tip' | 'action'

// AI 消息
export interface AIMessage {
  id: string
  role: MessageRole
  type: MessageType
  content: string
  timestamp: string
  // 附加数据（如推荐机构、护理建议等）
  metadata?: AIMessageMetadata
}

// 消息元数据
export interface AIMessageMetadata {
  // 推荐的机构ID列表
  recommendedInstitutions?: string[]
  // 护理建议
  careTip?: CareTip
  // 快捷操作
  actions?: QuickAction[]
}

// 护理建议
export interface CareTip {
  title: string
  content: string
  category: CareTipCategory
  icon?: string
}

export type CareTipCategory =
  | 'feeding'
  | 'grooming'
  | 'health'
  | 'exercise'
  | 'behavior'
  | 'general'

// 快捷操作
export interface QuickAction {
  id: string
  label: string
  action: string
  params?: Record<string, unknown>
}

// AI 会话
export interface AIConversation {
  id: string
  title: string
  messages: AIMessage[]
  createdAt: string
  updatedAt: string
}

// AI 会话摘要（用于列表展示）
export interface AIConversationSummary {
  id: string
  title: string
  lastMessage: string
  messageCount: number
  createdAt: string
  updatedAt: string
}

// 推荐问题
export interface SuggestedQuestion {
  id: string
  text: string
  category: QuestionCategory
}

export type QuestionCategory =
  | 'booking'
  | 'institution'
  | 'pet_care'
  | 'payment'
  | 'general'

// AI 响应请求
export interface AIRequestPayload {
  conversationId?: string
  message: string
  context?: AIContext
}

// AI 上下文
export interface AIContext {
  currentPage?: string
  selectedInstitutionId?: string
  selectedPetId?: string
  userPreferences?: UserPreferences
}

// 用户偏好
export interface UserPreferences {
  petTypes?: string[]
  priceRange?: { min: number; max: number }
  location?: { lat: number; lng: number }
}

// AI 响应
export interface AIResponse {
  message: AIMessage
  suggestedQuestions?: SuggestedQuestion[]
}

// AI 状态
export interface AIState {
  isOpen: boolean
  isTyping: boolean
  currentConversation: AIConversation | null
  conversations: AIConversationSummary[]
  suggestedQuestions: SuggestedQuestion[]
}
