import request from './request'

/** 获取可用的 AI 模型列表（含健康状态） */
export function getAvailableModels() {
  return request.get<any, Record<string, boolean>>('/ai/models')
}
