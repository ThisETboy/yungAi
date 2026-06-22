import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/auth'
import { getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: '',
    roles: [] as string[],
    permissions: [] as string[],
    menus: [] as any[],
  }),

  actions: {
    async login(username: string, password: string) {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      })
      const data = await res.json()
      if (data.code === 200) {
        this.token = data.data.accessToken
        localStorage.setItem('token', this.token)
        return true
      }
      return false
    },

    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
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
      this.username = ''
      this.roles = []
      this.permissions = []
      this.menus = []
      localStorage.removeItem('token')
    },
  },
})
