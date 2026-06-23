import type { Router } from 'vue-router'
import type { MenuNode } from '@/types/api'
import { useUserStore } from '@/store/user'

const layoutRoutes: any[] = []

function addDynamicRoutes(menus: MenuNode[], router: Router) {
  menus.forEach(menu => {
    if (menu.menuType === 2 && menu.routePath) {
      const route: any = {
        path: menu.routePath,
        name: menu.menuName,
        component: () => import(`@/views/${menu.component}.vue`),
        meta: { title: menu.menuName, perms: menu.perms },
      }
      layoutRoutes.push(route)
      router.addRoute('Layout', route)
    }
    if (menu.children && menu.children.length > 0) {
      addDynamicRoutes(menu.children, router)
    }
  })
}

export async function setupRouter(router: Router) {
  const userStore = useUserStore()
  const userInfo = await userStore.fetchUserInfo()

  if (userInfo && userInfo.menus && userInfo.menus.length > 0) {
    addDynamicRoutes(userInfo.menus, router)
  }
}
