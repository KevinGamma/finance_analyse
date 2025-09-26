package com.finance_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StockAnalysisRequest {

    @NotBlank(message = "Stock code is required")
    @Pattern(regexp = "^[A-Z]{4}$", message = "Stock code must be four uppercase characters e.g. AAPL")
    private String stockCode;

    @NotBlank(message = "Analysis type is required")
    @Pattern(regexp = "COMPREHENSIVE|STRUCTURED", message = "Analysis type must be COMPREHENSIVE or STRUCTURED")
    private String analysisType = "COMPREHENSIVE";

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
}
