import axios from 'axios';

const DEFAULT_TIMEOUT = 1_800_000; // 1800s = 30 minutes
const REQUEST_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT ?? DEFAULT_TIMEOUT);

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: REQUEST_TIMEOUT
});
