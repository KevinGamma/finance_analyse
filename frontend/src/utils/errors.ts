import type { AxiosError } from 'axios';

export function extractErrorMessage(error: unknown): string {
  if (typeof error === 'string' && error.trim()) {
    return error;
  }
  const axiosError = error as AxiosError<{ message?: string; errors?: Record<string, string> }>;
  if (axiosError?.response?.data?.message) {
    return axiosError.response.data.message;
  }
  if (axiosError.code === 'ECONNABORTED') {
    return '请求超时，请稍后重试。';
  }
  if (typeof axiosError.message === 'string' && axiosError.message.toLowerCase().includes('timeout')) {
    return '请求超时，请稍后重试。';
  }
  return '请求失败，请稍后重试。';
}
