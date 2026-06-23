import request from './request'

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export function sendChatSSE(messages: ChatMessage[], modelId?: number, provider?: string) {
  return fetch(`/api/ai/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      message: messages[messages.length - 1]?.content || '',
      modelId,
      provider,
    }),
  })
}

export function getAvailableModels() {
  return request.get<any, Record<string, boolean>>('/ai/models')
}
