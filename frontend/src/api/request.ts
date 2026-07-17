import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, getRefreshToken, removeToken, setToken, setRefreshToken } from '@/utils/auth'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 是否正在刷新 token 的标志位
let isRefreshing = false
// 等待刷新的请求队列
let pendingRequests: Array<(token: string) => void> = []

/**
 * 刷新 Token — 使用 refreshToken 换取新的 accessToken + refreshToken
 */
async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('No refresh token')
  }

  // 直接调用 axios，不经过拦截器，避免无限循环
  const response = await axios.post('/api/auth/refresh', null, {
    headers: {
      Authorization: `Bearer ${refreshToken}`,
    },
    timeout: 10000,
  })

  const data = response.data
  if (data.code === 200 && data.data) {
    setToken(data.data.accessToken)
    setRefreshToken(data.data.refreshToken)
    return data.data.accessToken
  }
  throw new Error(data.message || 'Refresh failed')
}

// Request interceptor: attach JWT token
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: handle 401 with auto-refresh
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  async (error) => {
    if (!error.response) {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(error)
    }

    const { status } = error.response
    const originalRequest = error.config

    // 如果是 401 且不是重试请求
    if (status === 401 && !originalRequest._retry) {
      // 如果已经在刷新 token，将请求加入队列
      if (isRefreshing) {
        return new Promise((resolve) => {
          pendingRequests.push((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(service(originalRequest))
          })
        })
      }

      // 标记正在刷新
      isRefreshing = true
      originalRequest._retry = true

      try {
        // 刷新 token
        const newToken = await refreshAccessToken()

        // 处理队列中的请求
        pendingRequests.forEach((callback) => callback(newToken))
        pendingRequests = []

        // 重试原请求
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return service(originalRequest)
      } catch (refreshError) {
        // 刷新失败，清除 token 并跳转登录
        removeToken()
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 其他错误
    if (status >= 500) {
      ElMessage.error('服务器错误，请稍后重试')
    } else if (status !== 401) {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
