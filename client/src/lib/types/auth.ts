export enum AuthErrorType {
  USER_NOT_FOUND = "USER_NOT_FOUND",
  INVALID_PASSWORD = "INVALID_PASSWORD",
  ACCOUNT_DISABLED = "ACCOUNT_DISABLED",
  ACCOUNT_LOCKED = "ACCOUNT_LOCKED",
  CREDENTIALS_EXPIRED = "CREDENTIALS_EXPIRED",
  EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS",
  VALIDATION_ERROR = "VALIDATION_ERROR",
  UNKNOWN_ERROR = "UNKNOWN_ERROR"
}

export interface AuthErrorResponse {
  error: string;
  message: string;
  errorType: AuthErrorType;
  timestamp: string;
  path?: string;
}

export interface LoginError {
  type: AuthErrorType;
  message: string;
  userFriendlyMessage: string;
} 