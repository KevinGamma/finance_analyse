package com.finance_analysis.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance_analysis.dto.NewsAnalysisRequest;
import com.finance_analysis.dto.NewsAnalysisResponse;
import com.finance_analysis.service.AuthService;
import com.finance_analysis.service.NewsAnalysisService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/news")
@Validated
public class NewsAnalysisController {

    private final NewsAnalysisService newsAnalysisService;
    private final AuthService authService;

    public NewsAnalysisController(NewsAnalysisService newsAnalysisService, AuthService authService) {
        this.newsAnalysisService = newsAnalysisService;
        this.authService = authService;
    }

    @PostMapping("/analysis")
    public NewsAnalysisResponse analyze(@Valid @RequestBody NewsAnalysisRequest request,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireUser(authorization);
        return newsAnalysisService.analyzeKeyword(request.getKeyword());
    }

    @GetMapping("/history")
    public List<NewsAnalysisResponse> history(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireUser(authorization);
        return newsAnalysisService.fetchRecentHistory();
    }
}
