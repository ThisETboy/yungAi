export interface WordItem {
  id?: number
  word: string
  popularity: number
  color?: string
  category: string
  source?: number
}

export interface CategoryItem {
  category: string
  count: number
}
