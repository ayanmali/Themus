import { useCallback } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { API_BASE_URL, PY_SERVICE_URL } from '@/lib/utils';

// Global auth state management
let isRefreshing = false;
let refreshPromise: Promise<boolean> | null = null;

const useApi = () => {
  const auth = useAuth();

  // Define endpoints that don't require authentication from the client
  const unauthenticatedEndpoints = [
    '/api/attempts/live/', // All candidate assessment endpoints
  ];

  const isUnauthenticatedEndpoint = (url: string): boolean => {
    return unauthenticatedEndpoints.some(endpoint => url.includes(endpoint));
  };

  const apiCall = useCallback(async (url: string, options: RequestInit = {}) => {
    // Skip authentication check for unauthenticated endpoints
    if (!isUnauthenticatedEndpoint(url) && !auth.isAuthenticated) {
      throw new Error('User not authenticated');
    }

    // Add auth headers
    const headers: Record<string, string> = {};
    
    // Copy existing headers
    if (options.headers) {
      if (options.headers instanceof Headers) {
        options.headers.forEach((value, key) => {
          headers[key] = value;
        });
      } 
      else if (Array.isArray(options.headers)) {
        options.headers.forEach(([key, value]) => {
          headers[key] = value;
        });
      } 
      else {
        Object.assign(headers, options.headers);
      }
    }
    
    // Only set Content-Type to application/json if not sending FormData
    if (!(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }

    const makeRequest = async (): Promise<Response> => {
      // TODO: add an API gateway to handle the requests to the different services?
      const fullUrl = `${url.includes('api/recordings') ? PY_SERVICE_URL : API_BASE_URL}${url}`;
      return await fetch(fullUrl, {
        ...options,
        credentials: 'include',
        headers,
      });
    };

    try {
      let response = await makeRequest();

      // Handle 401 responses with automatic token refresh (only for authenticated endpoints)
      if (response.status === 401 && !isUnauthenticatedEndpoint(url)) {
        console.log('Access token expired, attempting refresh...');

        // // Try to refresh the token
        // const refreshSuccess = await auth.refreshToken();

        // if (refreshSuccess) {
        //   console.log('Token refresh successful, retrying original request...');
        //   // Retry the original request with the new token
        //   response = await makeRequest();

        //   // If still 401 after refresh, redirect to login
        //   if (response.status === 401) {
        //     auth.logout();
        //     throw new Error('Authentication failed after token refresh');
        //   }
        // } else {
        //   // Refresh failed, redirect to login
        //   auth.logout();
        //   throw new Error('Token refresh failed');
        // }

        // Prevent multiple simultaneous refresh attempts
        if (!isRefreshing) {
          isRefreshing = true;
          refreshPromise = auth.refreshToken() || Promise.resolve(false);
        }

        // Wait for the refresh to complete
        const refreshSuccess = await refreshPromise;
        isRefreshing = false;
        refreshPromise = null;

        if (refreshSuccess) {
          console.log('Token refresh successful, retrying original request...');
          // Retry the original request with the new token
          response = await makeRequest();

          // If still 401 after refresh, logout
          if (response.status === 401) {
            auth.logout();
            throw new Error('Authentication failed after token refresh');
          }
        } else {
          // Refresh failed, logout
          auth.logout();
          throw new Error('Token refresh failed');
        }
      }

      if (!response.ok) {
        console.log('Response invalid:', response);
        // Try to parse error response for structured errors
        let errorData;
        try {
          errorData = await response.json();
        } catch {
          errorData = { message: `HTTP error! status: ${response.status}` };
        }
        
        // Create error object with status
        const error = { ...errorData, status: response.status };
        throw error;
      }

      // Handle 204 No Content responses (no body to parse)
      if (response.status === 204) {
        return null;
      }

      // Check if this is an SSE response
      const contentType = response.headers.get('content-type');
      console.log('🔍 Response content-type:', contentType);
      if (contentType && contentType.includes('text/event-stream')) {
        console.log('✅ Detected SSE response, returning response object directly');
        // Return the response object directly for SSE streams
        return response;
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