import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { login as apiLogin, register as apiRegister, logout as apiLogout } from '../api/auth'
import { setToken } from '../api/client'
import type { AuthResponse, CompanyRequest, LoginRequest } from '../api/types'

interface AuthState {
  token: string | null
  companyId: number | null
  email: string | null
  role: 'SUPPLIER' | 'BUYER' | null
}

interface AuthContextType extends AuthState {
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<void>
  register: (request: CompanyRequest) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    token: null,
    companyId: null,
    email: null,
    role: null,
  })

  const handleAuth = useCallback((response: AuthResponse) => {
    setState({
      token: response.token,
      companyId: response.companyId,
      email: response.email,
      role: response.role,
    })
  }, [])

  const login = useCallback(async (request: LoginRequest) => {
    const response = await apiLogin(request)
    handleAuth(response)
  }, [handleAuth])

  const register = useCallback(async (request: CompanyRequest) => {
    const response = await apiRegister(request)
    handleAuth(response)
  }, [handleAuth])

  const logout = useCallback(() => {
    apiLogout()
    setState({ token: null, companyId: null, email: null, role: null })
    setToken(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{
        ...state,
        isAuthenticated: !!state.token,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
