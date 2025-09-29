package com.finance_analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class IntradayTimeSeriesResponse {

    private String symbol;
    private String interval;
    private String lastRefreshed;
    private String timezone;
    private List<IntradayCandle> candles = new ArrayList<>();

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(String lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<IntradayCandle> getCandles() {
        return candles;
    }

    public void setCandles(List<IntradayCandle> candles) {
        this.candles = candles;
    }
}