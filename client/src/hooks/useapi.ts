// hooks/useApi.js
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

    try {
      const fullUrl = `${API_URL}${url}`;
      const response = await fetch(fullUrl, {
        ...options,
        credentials: 'include',
        headers,
      });

      // Handle 401 responses
      if (response.status === 401) {
        auth.logout();
        navigate('/login');
        throw new Error('Authentication failed');
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API call failed:', error);
      throw error;
    }
  }, [auth, navigate]);

  return { apiCall };
};

export default useApi;