import { useCallback } from 'react';
import { useAuth } from './use-auth';
import { navigate } from 'wouter/use-browser-location';
import { API_URL } from '@/lib/utils';

const useApi = () => {
  const auth = useAuth();

  const apiCall = useCallback(async (url: string, options: RequestInit = {}) => {
    // Check authentication before making request
    if (!auth.isAuthenticated) {
      navigate('/login');
      throw new Error('User not authenticated');
    }

    // Add auth headers
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    const makeRequest = async (): Promise<Response> => {
      const fullUrl = `${API_URL}${url}`;
      return await fetch(fullUrl, {
        ...options,
        credentials: 'include',
        headers,
      });
    };

    try {
      let response = await makeRequest();

      // Handle 401 responses with automatic token refresh
      if (response.status === 401) {
        console.log('Access token expired, attempting refresh...');
        
        // Try to refresh the token
        const refreshSuccess = await auth.refreshToken();
        
        if (refreshSuccess) {
          console.log('Token refresh successful, retrying original request...');
          // Retry the original request with the new token
          response = await makeRequest();
          
          // If still 401 after refresh, redirect to login
          if (response.status === 401) {
            auth.logout();
            throw new Error('Authentication failed after token refresh');
          }
        } else {
          // Refresh failed, redirect to login
          auth.logout();
          throw new Error('Token refresh failed');
        }
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API call failed:', error);
      throw error;
    }
  }, [auth]);

  return { apiCall };
};

export default useApi;