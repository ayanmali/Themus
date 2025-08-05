import { AuthErrorType, AuthErrorResponse, LoginError } from './types/auth';

export function handleAuthError(error: any): LoginError {
  console.log("Handling auth error:", error);
  
  // Check if it's our structured error response
  if (error && typeof error === 'object' && 'errorType' in error) {
    const authError = error as AuthErrorResponse;
    console.log("Structured auth error:", authError);
    
    switch (authError.errorType) {
      case AuthErrorType.USER_NOT_FOUND:
        return {
          type: AuthErrorType.USER_NOT_FOUND,
          message: authError.message,
          userFriendlyMessage: "No account found with this email address. Please check your email or sign up for a new account."
        };
        
      case AuthErrorType.INVALID_PASSWORD:
        return {
          type: AuthErrorType.INVALID_PASSWORD,
          message: authError.message,
          userFriendlyMessage: "Incorrect password. Please try again or reset your password."
        };
        
      case AuthErrorType.EMAIL_ALREADY_EXISTS:
        return {
          type: AuthErrorType.EMAIL_ALREADY_EXISTS,
          message: authError.message,
          userFriendlyMessage: "An account with this email address already exists. Please try logging in instead."
        };
        
      case AuthErrorType.VALIDATION_ERROR:
        return {
          type: AuthErrorType.VALIDATION_ERROR,
          message: authError.message,
          userFriendlyMessage: authError.message || "Please check your input and try again."
        };
        
      case AuthErrorType.ACCOUNT_DISABLED:
        return {
          type: AuthErrorType.ACCOUNT_DISABLED,
          message: authError.message,
          userFriendlyMessage: "Your account has been disabled. Please contact support for assistance."
        };
        
      case AuthErrorType.ACCOUNT_LOCKED:
        return {
          type: AuthErrorType.ACCOUNT_LOCKED,
          message: authError.message,
          userFriendlyMessage: "Your account has been locked. Please contact support for assistance."
        };
        
      case AuthErrorType.CREDENTIALS_EXPIRED:
        return {
          type: AuthErrorType.CREDENTIALS_EXPIRED,
          message: authError.message,
          userFriendlyMessage: "Your credentials have expired. Please reset your password."
        };
        
      default:
        return {
          type: AuthErrorType.UNKNOWN_ERROR,
          message: authError.message || "Unknown authentication error",
          userFriendlyMessage: "An unexpected error occurred. Please try again."
        };
    }
  }
  
  // Handle network errors or other unexpected errors
  if (error instanceof TypeError && error.message.includes('fetch')) {
    return {
      type: AuthErrorType.UNKNOWN_ERROR,
      message: "Network error",
      userFriendlyMessage: "Unable to connect to the server. Please check your internet connection and try again."
    };
  }
  
  // Handle HTTP status errors
  if (error && typeof error === 'object' && 'status' in error) {
    const status = (error as any).status;
    
    switch (status) {
      case 401:
        return {
          type: AuthErrorType.INVALID_PASSWORD,
          message: "Unauthorized",
          userFriendlyMessage: "Invalid email or password. Please try again."
        };
        
      case 403:
        return {
          type: AuthErrorType.ACCOUNT_DISABLED,
          message: "Forbidden",
          userFriendlyMessage: "Access denied. Your account may be disabled."
        };
        
      case 409:
        return {
          type: AuthErrorType.EMAIL_ALREADY_EXISTS,
          message: "Conflict",
          userFriendlyMessage: "An account with this email address already exists. Please try logging in instead."
        };
        
      case 500:
        return {
          type: AuthErrorType.UNKNOWN_ERROR,
          message: "Server error",
          userFriendlyMessage: "Server error. Please try again later."
        };
        
      default:
        return {
          type: AuthErrorType.UNKNOWN_ERROR,
          message: `HTTP ${status}`,
          userFriendlyMessage: "An unexpected error occurred. Please try again."
        };
    }
  }
  
  // Fallback for any other error
  return {
    type: AuthErrorType.UNKNOWN_ERROR,
    message: error?.message || "Unknown error",
    userFriendlyMessage: "An unexpected error occurred. Please try again."
  };
}

export function getFieldSpecificError(error: LoginError): { field: 'email' | 'password' | 'general', message: string } {
  switch (error.type) {
    case AuthErrorType.USER_NOT_FOUND:
    case AuthErrorType.EMAIL_ALREADY_EXISTS:
      return {
        field: 'email',
        message: error.userFriendlyMessage
      };
      
    case AuthErrorType.INVALID_PASSWORD:
      return {
        field: 'password',
        message: error.userFriendlyMessage
      };
      
    case AuthErrorType.VALIDATION_ERROR:
      // For validation errors, we'll show them as general errors since they could be any field
      return {
        field: 'general',
        message: error.userFriendlyMessage
      };
      
    default:
      return {
        field: 'general',
        message: error.userFriendlyMessage
      };
  }
} 