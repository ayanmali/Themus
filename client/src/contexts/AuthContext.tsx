import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { API_BASE_URL } from '@/lib/utils';
import { User } from '@/lib/types/user';
import { navigate } from 'wouter/use-browser-location';

interface AuthContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  isAuthenticated: boolean;
  setIsAuthenticated: (authenticated: boolean) => void;
  isLoading: boolean;
  checkAuth: () => Promise<void>;
  refreshToken: () => Promise<boolean>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isInitialized, setIsInitialized] = useState<boolean>(false);

  const refreshToken = useCallback(async (): Promise<boolean> => {
    console.log("Refreshing token...");
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
        method: 'POST',
        credentials: 'include'
      });
      
      if (response.ok) {
        console.log("Refresh successful, setting auth state");
        const userData = await response.json();
        setIsAuthenticated(true);
        setUser(userData);
        return true;
      }

      console.log("Refresh failed, clearing auth state");
      setIsAuthenticated(false);
      setUser(null);
      return false;
      
    } catch (error) {
      console.error('Token refresh failed:', error);
      setIsAuthenticated(false);
      setUser(null);
      return false;
    }
  }, []);

  const checkAuth = useCallback(async () => {
    // Prevent multiple simultaneous auth checks
    if (isLoading && isInitialized) {
      return;
    }

    console.log("Checking auth...");
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/is-authenticated`, {
        method: 'GET',
        credentials: 'include'
      });
      
      if (response.ok) {
        console.log("Auth check successful");
        const userData = await response.json();
        setIsAuthenticated(true);
        setUser(userData);
      } else if (response.status === 401) {
        console.log("Access token expired, attempting refresh...");
        // Try to refresh the token
        const refreshSuccess = await refreshToken();
        
        if (!refreshSuccess) {
          console.log("Refresh failed, user needs to log in");
          setIsAuthenticated(false);
          setUser(null);
        }
        // If refresh succeeds, auth state is already updated in refreshToken function
      } else if (response.status === 429) {
        console.log("Rate limit exceeded");
      } else {
        console.log("Auth check unsuccessful (non-401 error)");
        setIsAuthenticated(false);
        setUser(null);
      }
    } catch (error) {
      console.log("Error checking auth:", error);
      setIsAuthenticated(false);
      setUser(null);
    } finally {
      setIsLoading(false);
      setIsInitialized(true);
    }
  }, [refreshToken, isLoading, isInitialized]);

  const logout = useCallback(async () => {
    console.log("Logging out...");
    try {
      await fetch(`${API_BASE_URL}/api/auth/logout`, {
        method: 'POST',
        credentials: 'include'
      });
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      setIsAuthenticated(false);
      setUser(null);
      navigate("/login");
    }
  }, []);

  // Initialize auth state on mount
  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  // Register auth context with queryClient
  // useEffect(() => {
  //   setAuthContext({
  //     refreshToken,
  //     logout
  //   });
  // }, [refreshToken, logout]);

  const value: AuthContextType = {
    user,
    setUser,
    isAuthenticated,
    setIsAuthenticated,
    isLoading,
    checkAuth,
    refreshToken,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}; 