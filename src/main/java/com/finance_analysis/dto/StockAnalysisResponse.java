package com.finance_analysis.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public class StockAnalysisResponse {

    private Long id;
    private String stockCode;
    private String analysisType;
    private JsonNode analysis;
    private LocalDateTime requestedAt;

    public StockAnalysisResponse() {
    }

    public StockAnalysisResponse(Long id, String stockCode, String analysisType, JsonNode analysis, LocalDateTime requestedAt) {
        this.id = id;
        this.stockCode = stockCode;
        this.analysisType = analysisType;
        this.analysis = analysis;
        this.requestedAt = requestedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public JsonNode getAnalysis() {
        return analysis;
    }

    public void setAnalysis(JsonNode analysis) {
        this.analysis = analysis;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
