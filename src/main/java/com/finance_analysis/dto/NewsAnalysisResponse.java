package com.finance_analysis.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public class NewsAnalysisResponse {

    private Long id;
    private String keyword;
    private JsonNode analysis;
    private LocalDateTime requestedAt;

    public NewsAnalysisResponse() {
    }

    public NewsAnalysisResponse(Long id, String keyword, JsonNode analysis, LocalDateTime requestedAt) {
        this.id = id;
        this.keyword = keyword;
        this.analysis = analysis;
        this.requestedAt = requestedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
