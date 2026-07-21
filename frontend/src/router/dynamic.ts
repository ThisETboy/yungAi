import type { Router } from 'vue-router'
import type { MenuNode } from '@/types/api'
import { useUserStore } from '@/store/user'

/** 已注册的动态路由名称集合，用于登录后清理旧路由 */
const registeredRouteNames = new Set<string>()

/** 组件映射表 — key 对应菜单的 component 字段，value 是动态 import */
const componentMap: Record<string, () => Promise<any>> = {
  // 系统管理模块
  'system/user/UserManage': () => import('@/views/system/user/UserManage.vue'),
  'system/role/RoleManage': () => import('@/views/system/role/RoleManage.vue'),
  'system/menu/MenuManage': () => import('@/views/system/menu/MenuManage.vue'),
  // AI 模块
  'ai/chat/ChatView': () => import('@/views/ai/chat/ChatView.vue'),
  'ai/codegen/CodeGenView': () => import('@/views/ai/codegen/CodeGenView.vue'),
  'ai/modelconfig/AiModelConfigManage': () => import('@/views/ai/modelconfig/AiModelConfigManage.vue'),
  // 协议模块
  'protocol/ProtocolManage': () => import('@/views/protocol/ProtocolManage.vue'),
  // 监控模块
  'monitor/RequestLogManage': () => import('@/views/monitor/RequestLogManage.vue'),
  // 数据字典
  'system/dict/DictManage': () => import('@/views/system/dict/DictManage.vue'),
  // 操作日志
  'system/operlog/OperLogManage': () => import('@/views/system/operlog/OperLogManage.vue'),
  // 文件管理
  'system/file/FileManage': () => import('@/views/system/file/FileManage.vue'),
  // 系统配置
  'system/config/SysConfigManage': () => import('@/views/system/config/SysConfigManage.vue'),
  // 缓存管理
  'system/cache/CacheManage': () => import('@/views/system/cache/CacheManage.vue'),
  // 登录日志
  'system/loginlog/LoginLogManage': () => import('@/views/system/loginlog/LoginLogManage.vue'),
  // 个人中心
  'profile/ProfileView': () => import('@/views/profile/ProfileView.vue'),
  // 词云中心
  'cloud/WordCloudView': () => import('@/views/cloud/WordCloudView.vue'),
}

/**
 * 递归添加动态路由
 * 只添加 menuType === 2 的菜单项（页面级菜单）
 */
function addDynamicRoutes(menus: MenuNode[], router: Router) {
  menus.forEach(menu => {
    if (menu.menuType === 2 && menu.routePath) {
      // 从映射表获取组件，找不到则用空组件兜底
      const component = componentMap[menu.component] || (() => import('@/views/dashboard/Dashboard.vue'))
      const route: any = {
        path: menu.routePath,
        name: menu.menuName,
        component,
        meta: { title: menu.menuName, perms: menu.perms },
      }
      if (menu.menuName) {
        registeredRouteNames.add(menu.menuName)
      }
      router.addRoute('Layout', route)
    }
    // 递归处理子菜单
    if (menu.children && menu.children.length > 0) {
      addDynamicRoutes(menu.children, router)
    }
  })
}

/**
 * 清理旧动态路由 — 在添加新路由前调用
 * 防止重复登录导致路由重复注册
 */
function clearDynamicRoutes(router: Router) {
  registeredRouteNames.forEach(name => {
    try {
      router.removeRoute(name)
    } catch {
      // 路由可能已被移除，忽略
    }
  })
  registeredRouteNames.clear()
}

/**
 * 初始化动态路由
 * 1. 获取当前用户信息（包含权限和菜单）
 * 2. 清理旧路由（防止重复注册）
 * 3. 遍历菜单树，将 menuType=2 的菜单注册为动态路由
 * 4. 添加到 Layout 路由下
 */
export async function setupRouter(router: Router) {
  const userStore = useUserStore()
  const userInfo = await userStore.fetchUserInfo()

  // 清理旧路由
  clearDynamicRoutes(router)

  if (userInfo && userInfo.menus && userInfo.menus.length > 0) {
    addDynamicRoutes(userInfo.menus, router)
  }
}
