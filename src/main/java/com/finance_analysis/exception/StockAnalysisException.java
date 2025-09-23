package com.finance_analysis.exception;

public class StockAnalysisException extends RuntimeException {

    public StockAnalysisException(String message) {
        super(message);
    }

    public StockAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
