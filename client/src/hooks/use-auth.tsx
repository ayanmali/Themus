import { useState, useEffect } from 'react'
import { API_URL } from '@/lib/utils'
import { User } from '@/lib/types/user'

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(true)

  const checkAuth = async () => {
    try {
      const response = await fetch(`${API_URL}/api/users/is-authenticated`, {
        method: 'GET',
        credentials: 'include'
      });
      setIsAuthenticated(response.ok);
      response.ok && setUser(await response.json());
    } catch (error) {
      setIsAuthenticated(false)
      setUser(null);
    } finally {
      setIsLoading(false)
    }
  }

  const refreshToken = async (): Promise<boolean> => {
    try {
      const response = await fetch(`${API_URL}/api/auth/refresh`, {
        method: 'POST',
        credentials: 'include'
      });
      
      if (response.ok) {
        // Refresh successful, check auth status
        await checkAuth();
        return true;
      }
      
      // Refresh failed, clear auth state
      setIsAuthenticated(false);
      setUser(null);
      return false;
      
    } catch (error) {
      console.error('Token refresh failed:', error);
      setIsAuthenticated(false);
      setUser(null);
      return false;
    }
  };

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
      setUser(null);
    }
  }

  return {
    user,
    isAuthenticated,
    isLoading,
    checkAuth,
    refreshToken,
    logout
  }
}
