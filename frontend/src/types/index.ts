export interface StockAnalysisRequest {
  stockCode: string;
}

export interface StockAnalysisResponse {
  id?: number;
  stockCode: string;
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
