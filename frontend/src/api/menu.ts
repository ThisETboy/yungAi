import request from './request'
import type { MenuNode } from '@/types/api'

export function getMenuTree() {
  return request.get<any, MenuNode[]>('/menus/tree')
}

export function getUserMenus() {
  return request.get<any, MenuNode[]>('/menus/user')
}
