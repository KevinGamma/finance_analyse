import type { NewsAnalysisRequest, NewsAnalysisResponse } from '../types';
import { apiClient } from './client';

export async function analyzeNews(request: NewsAnalysisRequest): Promise<NewsAnalysisResponse> {
  const { data } = await apiClient.post<NewsAnalysisResponse>('/news/analysis', request);
  return data;
}

export async function fetchNewsHistory(): Promise<NewsAnalysisResponse[]> {
  const { data } = await apiClient.get<NewsAnalysisResponse[]>('/news/history');
  return data;
}
