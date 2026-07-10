import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'
import { setupRouter } from './dynamic'

const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页' },
      },
      // 协议管理页面 — 静态路由（不受动态菜单控制）
      {
        path: 'protocol',
        name: 'ProtocolManage',
        component: () => import('@/views/protocol/ProtocolManage.vue'),
        meta: { title: '协议管理' },
      },
      // 请求日志页面 — 静态路由
      {
        path: 'logs',
        name: 'RequestLogManage',
        component: () => import('@/views/monitor/RequestLogManage.vue'),
        meta: { title: '请求日志' },
      },
      // AI 模型配置页面 — 静态路由
      {
        path: 'ai-models',
        name: 'AiModelConfigManage',
        component: () => import('@/views/ai/modelconfig/AiModelConfigManage.vue'),
        meta: { title: 'AI模型配置' },
      },
      // 数据字典页面 — 静态路由
      {
        path: 'dict',
        name: 'DictManage',
        component: () => import('@/views/system/dict/DictManage.vue'),
        meta: { title: '数据字典' },
      },
      // 操作日志页面 — 静态路由
      {
        path: 'oper-log',
        name: 'OperLogManage',
        component: () => import('@/views/system/operlog/OperLogManage.vue'),
        meta: { title: '操作日志' },
      },
      // 个人中心页面 — 静态路由
      {
        path: 'profile',
        name: 'ProfileView',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心' },
      },
      // 文件管理页面 — 静态路由
      {
        path: 'files',
        name: 'FileManage',
        component: () => import('@/views/system/file/FileManage.vue'),
        meta: { title: '文件管理' },
      },
      // 系统配置页面 — 静态路由
      {
        path: 'config',
        name: 'SysConfigManage',
        component: () => import('@/views/system/config/SysConfigManage.vue'),
        meta: { title: '系统配置' },
      },
      // 缓存管理页面 — 静态路由
      {
        path: 'cache',
        name: 'CacheManage',
        component: () => import('@/views/system/cache/CacheManage.vue'),
        meta: { title: '缓存管理' },
      },
      // 登录日志页面 — 静态路由
      {
        path: 'login-log',
        name: 'LoginLogManage',
        component: () => import('@/views/system/loginlog/LoginLogManage.vue'),
        meta: { title: '登录日志' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在' },
  } as any,
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
})

router.beforeEach(async (to: any, _from: any, next: any) => {
  if (to.meta.public) {
    next()
    return
  }
  const token = getToken()
  if (!token) {
    next('/login')
    return
  }
  try {
    await setupRouter(router)
    next()
  } catch {
    next('/login')
  }
})

export default router
