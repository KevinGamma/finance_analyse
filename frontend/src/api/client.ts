import axios, { AxiosHeaders } from 'axios';
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios';

import { AUTH_TOKEN_KEY } from '../constants';

type UnauthorizedHandler = () => void;

const DEFAULT_TIMEOUT = 1_800_000; // 1800s = 30 minutes
const REQUEST_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT ?? DEFAULT_TIMEOUT);

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: REQUEST_TIMEOUT
});

const unauthorizedHandlers = new Set<UnauthorizedHandler>();

export function registerUnauthorizedHandler(handler: UnauthorizedHandler) {
  unauthorizedHandlers.add(handler);
}

export function removeUnauthorizedHandler(handler: UnauthorizedHandler) {
  unauthorizedHandlers.delete(handler);
}

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (typeof window !== 'undefined') {
    const token = window.localStorage.getItem(AUTH_TOKEN_KEY);
    if (token) {
      let headers = config.headers;
      if (!headers) {
        headers = new AxiosHeaders();
        config.headers = headers;
      }
      if (headers instanceof AxiosHeaders) {
        headers.set('Authorization', `Bearer ${token}`);
      } else {
        (headers as Record<string, string>).Authorization = `Bearer ${token}`;
      }
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    if (error?.response?.status === 401 && typeof window !== 'undefined') {
      window.localStorage.removeItem(AUTH_TOKEN_KEY);
      unauthorizedHandlers.forEach((handler) => handler());
    }
    return Promise.reject(error);
  }
);
