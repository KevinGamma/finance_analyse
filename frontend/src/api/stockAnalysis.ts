import type { StockAnalysisRequest, StockAnalysisResponse } from '../types';
import { apiClient } from './client';
import type { ExtractedJson, SingleStockApiResponse, SingleStockApiEnvelope, TimeSeriesRawMap, TimeSeriesCandle, StructuredWithSeriesResult, IntradaySeriesResponse, IntradayInterval } from '../types';

export async function analyzeStock(request: StockAnalysisRequest): Promise<StockAnalysisResponse> {
  const { data } = await apiClient.post<StockAnalysisResponse>('/stocks/analysis', request);
  return data;
}

export async function fetchHistory(): Promise<StockAnalysisResponse[]> {
  const { data } = await apiClient.get<StockAnalysisResponse[]>('/stocks/history');
  return data;
}

export async function fetchIntradaySeries(symbol: string, interval: IntradayInterval): Promise<IntradaySeriesResponse> {
  const { data } = await apiClient.get<IntradaySeriesResponse>('/stocks/intraday', { params: { symbol, interval } });
  return data;
}

export function mapIntradayToCandles(series: IntradaySeriesResponse): TimeSeriesCandle[] {
  return (series.candles ?? [])
    .map((c) => ({
      date: c.timestamp,
      open: Number(c.open ?? 0),
      high: Number(c.high ?? 0),
      low: Number(c.low ?? 0),
      close: Number(c.close ?? 0),
      volume: Number(c.volume ?? 0)
    }))
    .filter((c) => Number.isFinite(c.open) && Number.isFinite(c.high) && Number.isFinite(c.low) && Number.isFinite(c.close))
    .sort((a, b) => (a.date < b.date ? -1 : a.date > b.date ? 1 : 0));
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

export function extractTimeSeries(payload: SingleStockApiResponse): TimeSeriesCandle[] {
  let map: TimeSeriesRawMap | undefined;
  if (Array.isArray(payload)) {
    const first = payload.find((x) => (x as SingleStockApiEnvelope)?.timeSeries) as SingleStockApiEnvelope | undefined;
    map = first?.timeSeries;
  } else if (payload && typeof payload === 'object' && 'timeSeries' in (payload as any)) {
    map = (payload as SingleStockApiEnvelope).timeSeries;
  }
  if (!map || typeof map !== 'object') return [];
  return Object.entries(map)
    .map(([date, v]) => {
      const open = parseFloat(v['1. open'] ?? '');
      const high = parseFloat(v['2. high'] ?? '');
      const low = parseFloat(v['3. low'] ?? '');
      const close = parseFloat(v['4. close'] ?? '');
      const volume = parseFloat(v['5. volume'] ?? '');
      if ([open, high, low, close].some((n) => Number.isNaN(n))) return null;
      return { date, open, high, low, close, volume: Number.isNaN(volume) ? 0 : volume } as TimeSeriesCandle;
    })
    .filter((x): x is TimeSeriesCandle => !!x)
    .sort((a, b) => (a.date < b.date ? -1 : a.date > b.date ? 1 : 0));
}

export async function analyzeSingleStock(code: string): Promise<ExtractedJson> {
  const raw = await analyzeSingleStockRaw(code);
  return toExtractedJson(raw as SingleStockApiResponse);
}

export async function analyzeSingleStockWithSeries(code: string): Promise<StructuredWithSeriesResult> {
  const raw = await analyzeSingleStockRaw(code);
  const extracted = toExtractedJson(raw as SingleStockApiResponse);
  const candles = extractTimeSeries(raw as SingleStockApiResponse);
  return { extracted, candles };
}
