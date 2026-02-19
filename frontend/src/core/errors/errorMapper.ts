export interface AppError {
  message: string;
  code: string;
  status: number;
  path?: string;
  timestamp: string;
}

export const mapBackendError = (errorData: any): AppError => {
  if (errorData?.status === 429) {
    return {
      message: 'Too many requests. Please try again later.',
      code: 'RATE_LIMIT_EXCEEDED',
      status: 429,
      path: errorData.path,
      timestamp: errorData.timestamp || new Date().toISOString(),
    };
  }

  return {
    message: errorData.message || 'An unexpected error occurred',
    code: errorData.errorCode || 'UNKNOWN_ERROR',
    status: errorData.status || 500,
    path: errorData.path,
    timestamp: errorData.timestamp || new Date().toISOString(),
  };
};
