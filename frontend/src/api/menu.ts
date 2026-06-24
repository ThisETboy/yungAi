import request from './request'
import type { MenuNode } from '@/types/api'

/** 获取完整菜单树（用于菜单管理页面展示） */
export function getMenuTree() {
  return request.get<any, MenuNode[]>('/menus/tree')
}
