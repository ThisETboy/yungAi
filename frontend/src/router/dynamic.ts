import type { Router } from 'vue-router'
import type { MenuNode } from '@/types/api'
import { useUserStore } from '@/store/user'

const layoutRoutes: any[] = []

/**
 * 递归添加动态路由
 * 只添加 menuType === 2 的菜单项（页面级菜单）
 * 组件路径动态 import：system/user/UserManage → @/views/system/user/UserManage.vue
 */
function addDynamicRoutes(menus: MenuNode[], router: Router) {
  menus.forEach(menu => {
    if (menu.menuType === 2 && menu.routePath) {
      const route: any = {
        path: menu.routePath,
        name: menu.menuName,
        // 动态加载组件
        component: () => import(`@/views/${menu.component}.vue`),
        meta: { title: menu.menuName, perms: menu.perms },
      }
      layoutRoutes.push(route)
      router.addRoute('Layout', route)
    }
    // 递归处理子菜单
    if (menu.children && menu.children.length > 0) {
      addDynamicRoutes(menu.children, router)
    }
  })
}

/**
 * 初始化动态路由
 * 1. 获取当前用户信息（包含权限和菜单）
 * 2. 遍历菜单树，将 menuType=2 的菜单注册为动态路由
 * 3. 添加到 Layout 路由下
 */
export async function setupRouter(router: Router) {
  const userStore = useUserStore()
  const userInfo = await userStore.fetchUserInfo()

  if (userInfo && userInfo.menus && userInfo.menus.length > 0) {
    addDynamicRoutes(userInfo.menus, router)
  }
}
