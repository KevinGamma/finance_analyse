package com.finance_analysis.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping({"/", "/api"})
    public Map<String, Object> home() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("app", "Finance Analysis API");
        resp.put("status", "ok");
        resp.put("endpoints", new String[]{
            "/api/stocks/analysis [POST]",
            "/api/stocks/history [GET]",
            "/api/stocks/analyze?code=NVDA [GET]",
            "/api/news/analysis [POST]",
            "/api/news/history [GET]"
        });
        return resp;
    }
}

