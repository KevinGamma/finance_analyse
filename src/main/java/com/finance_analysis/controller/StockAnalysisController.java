package com.finance_analysis.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return stockAnalysisService.analyzeStockCode(stockCode, request.getAnalysisType());
    }

    @GetMapping("/history")
    public List<StockAnalysisResponse> history() {
        return stockAnalysisService.fetchRecentHistory();
    }

    @GetMapping(value = "/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> analyzeRaw(@RequestParam("code") String code) {
        String body = stockAnalysisService.analyzeSingleStockRaw(code);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
