package com.finance_analysis.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public class StockAnalysisResponse {

    private Long id;
    private String stockCode;
    private JsonNode analysis;
    private LocalDateTime requestedAt;

    public StockAnalysisResponse() {
    }

    public StockAnalysisResponse(Long id, String stockCode, JsonNode analysis, LocalDateTime requestedAt) {
        this.id = id;
        this.stockCode = stockCode;
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
