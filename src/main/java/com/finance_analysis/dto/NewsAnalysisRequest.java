package com.finance_analysis.dto;

import jakarta.validation.constraints.NotBlank;

public class NewsAnalysisRequest {

    @NotBlank(message = "Keyword is required")
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
