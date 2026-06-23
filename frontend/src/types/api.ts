export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
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

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  roles: string[]
  permissions: string[]
  menus: MenuNode[]
}

export interface UserInfoFromAuth {
  username: string
  roles: string[]
  permissions: string[]
  menus: MenuNode[]
}
