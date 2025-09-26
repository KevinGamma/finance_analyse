import type { StockAnalysisRequest, StockAnalysisResponse } from '../types';
import { apiClient } from './client';
import type { ExtractedJson, SingleStockApiResponse, SingleStockApiEnvelope } from '../types';

export async function analyzeStock(request: StockAnalysisRequest): Promise<StockAnalysisResponse> {
  const { data } = await apiClient.post<StockAnalysisResponse>('/stocks/analysis', request);
  return data;
}

export async function fetchHistory(): Promise<StockAnalysisResponse[]> {
  const { data } = await apiClient.get<StockAnalysisResponse[]>('/stocks/history');
  return data;
}

export async function analyzeSingleStockRaw(code: string): Promise<unknown> {
  const { data } = await apiClient.get('/stocks/analyze', { params: { code } });
  return data;
}

export function toExtractedJson(payload: SingleStockApiResponse): ExtractedJson {
  // Cases:
  // - Array of envelopes: [{ extractedJson: {...} }]
  // - Single envelope: { extractedJson: {...} }
  // - Direct object: { stock: ..., price_data: ... }
  if (Array.isArray(payload)) {
    const first = payload.find((x) => !!(x as SingleStockApiEnvelope)?.extractedJson) as SingleStockApiEnvelope | undefined;
    return (first?.extractedJson ?? {}) as ExtractedJson;
  }
  const maybeEnvelope = payload as SingleStockApiEnvelope;
  if (maybeEnvelope && typeof maybeEnvelope === 'object' && 'extractedJson' in maybeEnvelope) {
    return (maybeEnvelope.extractedJson ?? {}) as ExtractedJson;
  }
  return (payload as ExtractedJson) ?? {};
}

export async function analyzeSingleStock(code: string): Promise<ExtractedJson> {
  const raw = await analyzeSingleStockRaw(code);
  return toExtractedJson(raw as SingleStockApiResponse);
}

