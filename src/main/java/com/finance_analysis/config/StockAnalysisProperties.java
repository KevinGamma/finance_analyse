package com.finance_analysis.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stock.analysis")
public class StockAnalysisProperties {

    private URI comprehensiveUrl;
    private URI structuredUrl;

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
}
