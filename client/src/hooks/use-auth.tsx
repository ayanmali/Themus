import { useState, useEffect } from 'react'
import { API_URL } from '@/lib/utils'
import { User } from '@/lib/types/user'
import { navigate } from 'wouter/use-browser-location'

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [authStatus, setAuthStatus] = useState<number | null>(null);

  const checkAuth = async () => {
    console.log("Checking auth...")
    try {
      const response = await fetch(`${API_URL}/api/users/is-authenticated`, {
        method: 'GET',
        credentials: 'include'
      });
      
      if (response.ok) {
        console.log("Auth check successful")
        const userData = await response.json();
        setIsAuthenticated(true);
        setUser(userData);
      } else {
        console.log("Auth check unsuccessful")
        setIsAuthenticated(false);
        setUser(null);
        setAuthStatus(response.status);
      }
    } catch (error) {
      console.log("Error checking auth:", error)
      if (authStatus !== 429) {
        console.log("Auth check unsuccessful, setting auth to false")
        setIsAuthenticated(false)
        setUser(null);
      }
      else {
        console.log("Rate limit exceeded")
      }
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    checkAuth();
  }, []);

  const refreshToken = async (): Promise<boolean> => {
    console.log("Refreshing token...")
    try {
      const response = await fetch(`${API_URL}/api/auth/refresh`, {
        method: 'POST',
        credentials: 'include'
      });
      
      if (response.ok) {
        // Refresh successful, check auth status
        console.log("Refresh successful, checking auth status")
        await checkAuth();
        return true;
      }

      // Refresh failed, clear auth state
      console.log("Refresh failed, clearing auth state")
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

  const logout = async () => {
    console.log("Logging out...");
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
      navigate("/login")
    }
  }

  return {
    user,
    setUser,
    isAuthenticated,
    setIsAuthenticated,
    isLoading,
    checkAuth,
    refreshToken,
    logout
  }
}
