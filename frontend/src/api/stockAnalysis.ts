import axios from 'axios';

import type { StockAnalysisRequest, StockAnalysisResponse } from '../types';

const REQUEST_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT ?? 60000);

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: REQUEST_TIMEOUT
});

export async function analyzeStock(request: StockAnalysisRequest): Promise<StockAnalysisResponse> {
  const { data } = await apiClient.post<StockAnalysisResponse>('/stocks/analysis', request);
  return data;
}

export async function fetchHistory(): Promise<StockAnalysisResponse[]> {
  const { data } = await apiClient.get<StockAnalysisResponse[]>('/stocks/history');
  return data;
}
