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
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/login/Login.vue'),
  },
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
