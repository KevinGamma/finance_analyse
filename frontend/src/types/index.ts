export interface UserProfile {
  id: number;
  username: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  user: UserProfile;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload {
  username: string;
  password: string;
}

export interface StockAnalysisRequest {
  stockCode: string;
  analysisType: "COMPREHENSIVE" | "STRUCTURED";
}

export interface StockAnalysisResponse {
  id?: number;
  stockCode: string;
  analysisType: "COMPREHENSIVE" | "STRUCTURED";
  analysis: unknown;
  requestedAt: string;
}

export interface NewsAnalysisRequest {
  keyword: string;
}

export interface NewsAnalysisResponse {
  id?: number;
  keyword: string;
  analysis: unknown;
  requestedAt: string;
}

// New types for single-stock analysis (extractedJson)
export interface ExtractedJson {
  stock?: {
    code?: string;
    name?: string;
    market?: string;
    currency?: string;
    industry?: string;
    sector?: string;
  };
  date?: string;
  timezone?: string;
  price_data?: {
    open?: number;
    close?: number;
    high?: number;
    low?: number;
    prev_close?: number;
    volume?: number;
    avg_volume_20d?: number;
    daily_change?: number;
    daily_change_percent?: number;
  };
  technical_analysis?: {
    candle?: {
      type?: string;
      upper_shadow?: number;
      lower_shadow?: number;
      body_size?: number;
    };
    trend?: {
      short_term?: string;
      medium_term?: string;
      long_term?: string;
      notes?: string;
    };
    support_resistance?: {
      support?: Array<{ level?: number; strength?: string }>;
      resistance?: Array<{ level?: number; strength?: string }>;
    };
    indicators?: {
      moving_averages?: { ma5?: number; ma10?: number; ma20?: number; trend_signal?: string };
      rsi?: { value?: number; interpretation?: string };
      macd?: { macd_line?: number; signal_line?: number; histogram?: number; trend_signal?: string };
      bollinger_bands?: { upper?: number; middle?: number; lower?: number; position?: string };
    };
    volatility?: { daily_range_percent?: number; atr_14d?: number };
  };
  volume_analysis?: {
    daily_volume?: number;
    avg_volume_20d?: number;
    volume_trend?: string;
    volume_spike?: boolean;
    interpretation?: string;
  };
  fundamental_analysis?: {
    market_cap?: string | number;
    pe_ratio?: number;
    eps?: number;
    dividend_yield?: number;
    revenue_trend?: string;
    profit_margin?: number;
    notes?: string;
  };
  news_sentiment?: {
    news_count?: number;
    positive?: number;
    neutral?: number;
    negative?: number;
    overall_sentiment?: string;
    top_headlines?: Array<{ title?: string; sentiment?: string }>;
  };
  investment_advice?: {
    short_term?: {
      strategy?: string;
      buy_zone?: number[];
      take_profit_zone?: number[];
      stop_loss?: number;
      risk_level?: string;
    };
    medium_term?: {
      strategy?: string;
      buy_zone?: number[];
      take_profit_zone?: number[];
      stop_loss?: number;
      risk_level?: string;
    };
    long_term?: { strategy?: string; risk_notes?: string };
    risk_warning?: string;
  };
  macro_economic_factors?: {
    interest_rate?: number;
    inflation_rate?: number;
    market_sentiment?: string;
    notes?: string;
  };
  analysis_metadata?: {
    analysis_timestamp?: string;
    analyst?: string;
    data_sources?: string[];
    confidence_level?: string;
  };
}

export interface AnalysisTimeSeriesEntry {
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface TimeSeriesCandle {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export type TimeSeriesRawMap = Record<string, {
  '1. open'?: string; // using strings to reflect raw payload
  '2. high'?: string;
  '3. low'?: string;
  '4. close'?: string;
  '5. volume'?: string;
  [k: string]: string | undefined;
}>;

export type SingleStockApiEnvelope = { extractedJson?: ExtractedJson; timeSeries?: TimeSeriesRawMap; metaData?: unknown };
export type SingleStockApiResponse = ExtractedJson | SingleStockApiEnvelope | SingleStockApiEnvelope[];


export type IntradayInterval = "1min" | "5min" | "15min" | "30min" | "60min";

export interface IntradayCandleDto {
  timestamp: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface IntradaySeriesResponse {
  symbol: string;
  interval: IntradayInterval;
  lastRefreshed: string | null;
  timezone: string | null;
  candles: IntradayCandleDto[];
}

export interface CandlestickPoint {
  timestamp: string;
  label: string;
  open: number;
  close: number;
  low: number;
  high: number;
  volume: number;
}

export interface StructuredWithSeriesResult {
  extracted: ExtractedJson;
  candles: TimeSeriesCandle[];
}
