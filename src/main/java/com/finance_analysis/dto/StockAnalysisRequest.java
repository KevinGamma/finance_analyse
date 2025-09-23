package com.finance_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StockAnalysisRequest {

    @NotBlank(message = "Stock code is required")
    @Pattern(regexp = "^[A-Z]{4}$", message = "Stock code must be four uppercase characters (e.g. AAPL)")
    private String stockCode;

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
}
