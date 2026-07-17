export function getToken(): string | null {
  return localStorage.getItem('token')
}

export function setToken(token: string) {
  localStorage.setItem('token', token)
}

export function removeToken() {
  localStorage.removeItem('token')
}

// ---- Refresh Token ----
export function getRefreshToken(): string | null {
  return localStorage.getItem('refreshToken')
}

export function setRefreshToken(token: string) {
  localStorage.setItem('refreshToken', token)
}

export function removeRefreshToken() {
  localStorage.removeItem('refreshToken')
}

// ---- 清除所有认证信息 ----
export function clearAuth() {
  removeToken()
  removeRefreshToken()
}
