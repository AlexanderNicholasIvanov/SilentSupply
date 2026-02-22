import { apiClient, setToken } from './client'
import type { AuthResponse, CompanyRequest, LoginRequest } from './types'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const response = await apiClient<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  })
  setToken(response.token)
  return response
}

export async function register(request: CompanyRequest): Promise<AuthResponse> {
  const response = await apiClient<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(request),
  })
  setToken(response.token)
  return response
}

export function logout() {
  setToken(null)
}
