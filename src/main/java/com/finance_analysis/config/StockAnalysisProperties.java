package com.finance_analysis.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stock.analysis")
public class StockAnalysisProperties {

    private URI comprehensiveUrl;
    private URI structuredUrl;
    private URI alphaVantageUrl = URI.create("https://www.alphavantage.co/query");
    private String alphaVantageApiKey;
    private String intradayOutputSize = "compact";

    public URI getComprehensiveUrl() {
        return comprehensiveUrl;
    }

    public void setComprehensiveUrl(URI comprehensiveUrl) {
        this.comprehensiveUrl = comprehensiveUrl;
    }

    public URI getStructuredUrl() {
        return structuredUrl;
    }

    public void setStructuredUrl(URI structuredUrl) {
        this.structuredUrl = structuredUrl;
    }

    public URI getAlphaVantageUrl() {
        return alphaVantageUrl;
    }

    public void setAlphaVantageUrl(URI alphaVantageUrl) {
        this.alphaVantageUrl = alphaVantageUrl;
    }

    public String getAlphaVantageApiKey() {
        return alphaVantageApiKey;
    }

    public void setAlphaVantageApiKey(String alphaVantageApiKey) {
        this.alphaVantageApiKey = alphaVantageApiKey;
    }

    public String getIntradayOutputSize() {
        return intradayOutputSize;
    }

    public void setIntradayOutputSize(String intradayOutputSize) {
        this.intradayOutputSize = intradayOutputSize;
    }
}