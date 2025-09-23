package com.finance_analysis.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance_analysis.dto.StockAnalysisRequest;
import com.finance_analysis.dto.StockAnalysisResponse;
import com.finance_analysis.service.StockAnalysisService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stocks")
@Validated
public class StockAnalysisController {

    private final StockAnalysisService stockAnalysisService;

    public StockAnalysisController(StockAnalysisService stockAnalysisService) {
        this.stockAnalysisService = stockAnalysisService;
    }

    @PostMapping("/analysis")
    public StockAnalysisResponse analyze(@Valid @RequestBody StockAnalysisRequest request) {
        String stockCode = request.getStockCode().toUpperCase();
        return stockAnalysisService.analyzeStockCode(stockCode);
    }

    @GetMapping("/history")
    public List<StockAnalysisResponse> history() {
        return stockAnalysisService.fetchRecentHistory();
    }
}
