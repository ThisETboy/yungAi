import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// Request interceptor: attach JWT token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: unwrap response envelope
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
