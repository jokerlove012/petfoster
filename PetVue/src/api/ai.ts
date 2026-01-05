import api from './index'
import type { ApiResponse } from '@/types/common'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface ChatResponse {
  success: boolean
  content: string
  mock?: boolean
  error?: string
  usage?: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }
}

export const aiApi = {
  /**
   * 发送消息给AI助手
   * @param message 用户消息
   * @param history 历史消息（可选）
   */
  chat(message: string, history?: ChatMessage[]): Promise<ApiResponse<ChatResponse>> {
    return api.post('/ai/chat', { message, history })
  }
}
