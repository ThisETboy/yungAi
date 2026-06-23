import request from './request'
import type { LoginRequest, LoginResponse, UserInfoFromAuth } from '@/types/api'

export function login(data: LoginRequest) {
  return request.post<any, LoginResponse>('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function refreshToken() {
  return request.post<any, LoginResponse>('/auth/refresh')
}

export function getUserInfo() {
  return request.get<any, UserInfoFromAuth>('/auth/info')
}
