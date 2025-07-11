import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"
import { navigate } from "wouter/use-browser-location"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const API_URL = "http://localhost:8080"

export const isAuthenticated = async () => {
  const response = await fetch(`${API_URL}/api/auth/is-authenticated`, {
    method: 'GET',
    credentials: 'include'
  });
  return response.ok;
}

// Authentication token utilities
// export const authUtils = {
//   // Store access token in cookie
//   setAccessToken: (token: string, expiresInMinutes: number = import.meta.env.VITE_JWT_ACCESS_EXPIRATION) => {
//     const expirationDate = new Date()
//     expirationDate.setTime(expirationDate.getTime() + (expiresInMinutes * 60 * 1000))
//     document.cookie = `accessToken=${token}; expires=${expirationDate.toUTCString()}; path=/; secure; samesite=strict`
//   },

//   // Get access token from cookie
//   getAccessToken: (): string | null => {
//     const cookies = document.cookie.split(';')
//     const tokenCookie = cookies.find(cookie => cookie.trim().startsWith('accessToken='))
//     return tokenCookie ? tokenCookie.split('=')[1] : null
//   },

//   // Remove access token (logout)
//   removeAccessToken: () => {
//     document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; secure; samesite=strict'
//   },

//   // Check if user is authenticated
//   isAuthenticated: (): boolean => {
//     return !!authUtils.getAccessToken()
//   }
// }

// Authenticated fetch wrapper
// export const authenticatedFetch = async (url: string, options: RequestInit = {}): Promise<Response> => {
//   const token = authUtils.getAccessToken();

//   // if no access token, redirect to login
//   !token && navigate('/login');
  
//   const defaultHeaders: HeadersInit = {
//     'Content-Type': 'application/json',
//     ...(token && { 'Authorization': `Bearer ${token}` })
//   }

//   const mergedOptions: RequestInit = {
//     ...options,
//     headers: {
//       ...defaultHeaders,
//       ...options.headers
//     }
//   }

//   const response = await fetch(url, mergedOptions)
  
//   // If token is expired or invalid, remove it and potentially redirect to login
//   if (response.status === 401) {
//     authUtils.removeAccessToken()
//     // You might want to redirect to login page here
//     // window.location.href = '/login'
//   }

//   return response
// }

// Example usage for authenticated API calls:
/*
// Making authenticated API calls:

// 1. Simple GET request
const fetchUserProfile = async () => {
  try {
    const response = await authenticatedFetch(`${API_URL}/api/user/profile`)
    if (response.ok) {
      const userProfile = await response.json()
      return userProfile
    }
  } catch (error) {
    console.error('Failed to fetch user profile:', error)
  }
}

// 2. POST request with data
const createAssessment = async (assessmentData: any) => {
  try {
    const response = await authenticatedFetch(`${API_URL}/api/assessments`, {
      method: 'POST',
      body: JSON.stringify(assessmentData)
    })
    if (response.ok) {
      const newAssessment = await response.json()
      return newAssessment
    }
  } catch (error) {
    console.error('Failed to create assessment:', error)
  }
}

// 3. Check authentication status
const checkAuth = () => {
  if (authUtils.isAuthenticated()) {
    console.log('User is logged in')
  } else {
    console.log('User is not logged in')
    // Redirect to login
  }
}

// 4. Logout function
const logout = () => {
  authUtils.removeAccessToken()
  window.location.href = '/login'
}
*/

export const minutesToHours = (minutes: number) => {
  return Math.floor(minutes / 60);
}
