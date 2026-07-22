import request from './request'

export function getWordCloud(category?: string, startDate?: string, endDate?: string) {
  return request.get('/cloud/words', { params: { category, startDate, endDate } })
}

export function getCategories() {
  return request.get('/cloud/categories')
}

export function pageWords(params: any) {
  return request.get('/cloud/words/page', { params })
}

export function addWord(data: any) {
  return request.post('/cloud/words', data)
}

export function updateWord(id: number, data: any) {
  return request.put(`/cloud/words/${id}`, data)
}

export function deleteWord(id: number) {
  return request.delete(`/cloud/words/${id}`)
}

export function extractKeywords(text: string, limit?: number) {
  return request.post('/cloud/ai-extract', { text, limit })
}
