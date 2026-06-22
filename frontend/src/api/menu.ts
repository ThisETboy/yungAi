import request from './request'

export function getMenuTree() {
  return request.get<any, MenuNode[]>('/menus/tree')
}

export function getUserMenus() {
  return request.get<any, MenuNode[]>('/menus/user')
}

export interface MenuNode {
  id: number
  parentId: number
  menuType: number
  menuName: string
  routePath: string
  component: string
  icon: string
  sortOrder: number
  perms: string
  visible: number
  children?: MenuNode[]
}
