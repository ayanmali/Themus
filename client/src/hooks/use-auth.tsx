import { useState, useEffect } from 'react'
import { API_URL } from '@/lib/utils'

export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(true)

  const checkAuth = async () => {
    try {
      const response = await fetch(`${API_URL}/api/auth/is-authenticated`, {
        method: 'GET',
        credentials: 'include'
      })
      setIsAuthenticated(response.ok)
    } catch (error) {
      setIsAuthenticated(false)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    checkAuth()
  }, [])

  const logout = async () => {
    try {
      await fetch(`${API_URL}/api/auth/logout`, {
        method: 'POST',
        credentials: 'include'
      })
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      setIsAuthenticated(false)
    }
  }

  return {
    isAuthenticated,
    isLoading,
    checkAuth,
    logout
  }
}
