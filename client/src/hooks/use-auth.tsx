import { useState, useEffect } from 'react'
import { API_URL } from '@/lib/utils'
import { User } from '@/lib/types/user'
import { navigate } from 'wouter/use-browser-location'

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(true)

  const refreshToken = async (): Promise<boolean> => {
    console.log("Refreshing token...")
    try {
      const response = await fetch(`${API_URL}/api/auth/refresh`, {
        method: 'POST',
        credentials: 'include'
      });
      
      if (response.ok) {
        // Refresh successful, set auth state
        console.log("Refresh successful, setting auth state")
        setIsAuthenticated(true);
        setUser(await response.json());
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
      } else if (response.status === 401) {
        console.log("Access token expired, attempting refresh...")
        // Try to refresh the token
        const refreshSuccess = await refreshToken();
        
        if (!refreshSuccess) {
          console.log("Refresh failed, user needs to log in")
          setIsAuthenticated(false);
          setUser(null);
        }
        // If refresh succeeds, auth state is already updated in refreshToken function
      } else if (response.status === 429) {
        console.log("Rate limit exceeded");
      } else {
        console.log("Auth check unsuccessful (non-401 error)")
        setIsAuthenticated(false);
        setUser(null);
      }
    } catch (error) {
      console.log("Error checking auth:", error)
      setIsAuthenticated(false);
      setUser(null);
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    checkAuth();
  }, []);

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
