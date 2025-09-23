package com.finance_analysis.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stock.analysis")
public class StockAnalysisProperties {

    private URI n8nUrl;

    public URI getN8nUrl() {
        return n8nUrl;
    }

    public void setN8nUrl(URI n8nUrl) {
        this.n8nUrl = n8nUrl;
    }
}
