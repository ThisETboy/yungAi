import { getToken, setToken, removeToken } from '@/utils/auth'
import type { RouteRecordRaw } from 'vue-router'
import type { MenuNode, UserInfo } from '@/types/api'
import { getMenuTree } from '@/api/menu'

const layoutRoutes: RouteRecordRaw[] = []

function addDynamicRoutes(menus: MenuNode[]) {
  menus.forEach(menu => {
    if (menu.menuType === 2 && menu.routePath) {
      const route: RouteRecordRaw = {
        path: menu.routePath,
        name: menu.menuName,
        component: () => import(`@/views/${menu.component}.vue`),
        meta: { title: menu.menuName, perms: menu.perms },
      }
      layoutRoutes.push(route)
    }
    if (menu.children && menu.children.length > 0) {
      addDynamicRoutes(menu.children)
    }
  })
}

export async function setupRouter(router: any) {
  const userStore = useUserStore()
  const userInfo = await userStore.fetchUserInfo()

  if (userInfo) {
    addDynamicRoutes(userInfo.menus)
    layoutRoutes.forEach(route => {
      router.addRoute('Layout', route)
    })
  }
}

function useUserStore() {
  return {
    async fetchUserInfo(): Promise<UserInfo | null> {
      try {
        const token = getToken()
        if (!token) return null
        const res = await fetch('/api/auth/info', {
          headers: { Authorization: `Bearer ${token}` },
        })
        if (!res.ok) return null
        const data = await res.json()
        if (data.code !== 200) return null
        return data.data
      } catch {
        return null
      }
    },
  }
}

export interface UserInfo {
  username: string
  roles: string[]
  permissions: string[]
  menus: MenuNode[]
}
