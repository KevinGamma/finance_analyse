export interface StockAnalysisRequest {
  stockCode: string;
}

export interface StockAnalysisResponse {
  id?: number;
  stockCode: string;
  analysis: unknown;
  requestedAt: string;
}
