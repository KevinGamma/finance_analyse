package com.finance_analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.finance_analysis.config.NewsAnalysisProperties;
import com.finance_analysis.config.StockAnalysisProperties;

@SpringBootApplication
@EnableConfigurationProperties({StockAnalysisProperties.class, NewsAnalysisProperties.class})
public class FinanceAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceAnalysisApplication.class, args);
    }
}
