import type { StockAnalysisRequest, StockAnalysisResponse } from '../types';
import { apiClient } from './client';

export async function analyzeStock(request: StockAnalysisRequest): Promise<StockAnalysisResponse> {
  const { data } = await apiClient.post<StockAnalysisResponse>('/stocks/analysis', request);
  return data;
}

export async function fetchHistory(): Promise<StockAnalysisResponse[]> {
  const { data } = await apiClient.get<StockAnalysisResponse[]>('/stocks/history');
  return data;
}
