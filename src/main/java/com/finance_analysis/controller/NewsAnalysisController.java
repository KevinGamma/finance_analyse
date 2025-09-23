package com.finance_analysis.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance_analysis.dto.NewsAnalysisRequest;
import com.finance_analysis.dto.NewsAnalysisResponse;
import com.finance_analysis.service.NewsAnalysisService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/news")
@Validated
public class NewsAnalysisController {

    private final NewsAnalysisService newsAnalysisService;

    public NewsAnalysisController(NewsAnalysisService newsAnalysisService) {
        this.newsAnalysisService = newsAnalysisService;
    }

    @PostMapping("/analysis")
    public NewsAnalysisResponse analyze(@Valid @RequestBody NewsAnalysisRequest request) {
        return newsAnalysisService.analyzeKeyword(request.getKeyword());
    }

    @GetMapping("/history")
    public List<NewsAnalysisResponse> history() {
        return newsAnalysisService.fetchRecentHistory();
    }
}
