import { QueryClient, QueryFunction } from "@tanstack/react-query";
import { API_BASE_URL } from "./utils";
import useApi from "@/hooks/use-api";

// Global auth state management
// let isRefreshing = false;
// let refreshPromise: Promise<boolean> | null = null;

// Auth context integration
// let authContext: {
//   refreshToken: () => Promise<boolean>;
//   logout: () => void;
// } | null = null;

// Function to set auth context (called from AuthContext)
// export const setAuthContext = (context: {
//   refreshToken: () => Promise<boolean>;
//   logout: () => void;
// }) => {
//   authContext = context;
// };

// async function throwIfResNotOk(res: Response) {
//   if (!res.ok) {
//     const text = (await res.text()) || res.statusText;
//     throw new Error(`${res.status}: ${text}`);
//   }
// }

// export async function apiRequest(
//   method: string,
//   url: string,
//   data?: unknown | undefined,
// ): Promise<Response> {

//   // Ensure URL starts with http/https or prepend API base URL
//   // TODO: add an API gateway to handle the requests to the different services?
//   // TODO: use query client to fetch data from client side requests 
//   const fullUrl = url.startsWith('http') ? url : 
//   `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;

//   const headers: Record<string, string> = {};
  
//   // Only set Content-Type for JSON data, not for FormData
//   if (data && !(data instanceof FormData)) {
//     headers["Content-Type"] = "application/json";
//   }

//   const makeRequest = async (): Promise<Response> => {
//     return await fetch(fullUrl, {
//       method,
//       headers,
//       body: data instanceof FormData ? data : (data ? JSON.stringify(data) : undefined),
//       credentials: "include",
//     });
//   };

//   try {
//     let response = await makeRequest();

//     // Handle 401 responses with automatic token refresh
//     if (response.status === 401) {
//       console.log('Access token expired, attempting refresh...');
      
//       // Prevent multiple simultaneous refresh attempts
//       if (!isRefreshing) {
//         isRefreshing = true;
//         refreshPromise = authContext?.refreshToken() || Promise.resolve(false);
//       }
      
//       // Wait for the refresh to complete
//       const refreshSuccess = await refreshPromise;
//       isRefreshing = false;
//       refreshPromise = null;
      
//       if (refreshSuccess) {
//         console.log('Token refresh successful, retrying original request...');
//         // Retry the original request with the new token
//         response = await makeRequest();
        
//         // If still 401 after refresh, logout
//         if (response.status === 401) {
//           authContext?.logout();
//           throw new Error('Authentication failed after token refresh');
//         }
//       } else {
//         // Refresh failed, logout
//         authContext?.logout();
//         throw new Error('Token refresh failed');
//       }
//     }

//     await throwIfResNotOk(response);
//     return response;
//   } catch (error) {
//     console.error('API request failed:', error);
//     throw error;
//   }
// }

type UnauthorizedBehavior = "returnNull" | "throw";

export const getQueryFn: <T>(options: {
  on401: UnauthorizedBehavior;
}) => QueryFunction<T> =
  ({ on401: unauthorizedBehavior }) =>
  async ({ queryKey }) => {
    const { apiCall } = useApi();

    const url = queryKey.join("/") as string;
    const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
    
    const response = await apiCall(fullUrl, {
      method: "GET",
    });
    return response; // apiCall already returns the parsed JSON
  };

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      queryFn: getQueryFn({ on401: "throw" }),
      refetchInterval: false,
      refetchOnWindowFocus: false,
      staleTime: Infinity,
      retry: false,
    },
    mutations: {
      retry: false,
    },
  },
});
