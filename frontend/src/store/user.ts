import { defineStore } from 'pinia'
import { getUserInfo, login as loginApi } from '@/api/auth'
import { getToken, setToken, getRefreshToken, setRefreshToken, clearAuth } from '@/utils/auth'
import type { UserInfoFromAuth } from '@/types/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    refreshToken: getRefreshToken() || '',
    username: '',
    roles: [] as string[],
    permissions: [] as string[],
    menus: [] as any[],
    userInfo: {} as any,
  }),

  actions: {
    async login(username: string, password: string) {
      try {
        const res = await loginApi({ username, password })
        this.token = res.accessToken
        this.refreshToken = res.refreshToken
        setToken(this.token)
        setRefreshToken(this.refreshToken)
        return true
      } catch {
        return false
      }
    },

    async fetchUserInfo() {
      try {
        const res = await getUserInfo() as UserInfoFromAuth
        this.username = res.username
        this.roles = res.roles || []
        this.permissions = res.permissions || []
        this.menus = res.menus || []
        return res
      } catch {
        return null
      }
    },

    logout() {
      this.token = ''
      this.refreshToken = ''
      this.username = ''
      this.roles = []
      this.permissions = []
      this.menus = []
      clearAuth()
    },
  },
})
