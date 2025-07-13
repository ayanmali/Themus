import { useState, useEffect } from 'react'
import { API_URL } from '@/lib/utils'
import { User } from '@/lib/types/user'
import { navigate } from 'wouter/use-browser-location'

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(true)

  const checkAuth = async () => {
    console.log("Checking auth...")
    try {
      const response = await fetch(`${API_URL}/api/users/is-authenticated`, {
        method: 'GET',
        credentials: 'include'
      });
      response.ok ? console.log("Auth check successful") : console.log("Auth check unsuccessful")
      setIsAuthenticated(response.ok);
      response.ok && setUser(await response.json());
    } catch (error) {
      console.log("Error checking auth:", error)
      if (error instanceof Error && !error.message.includes("rate limit")) {
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
      navigate("/login")
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      setIsAuthenticated(false)
      setUser(null);
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
