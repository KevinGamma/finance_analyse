package com.finance_analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.finance_analysis.mapper.StockAnalysisMapper;
import com.finance_analysis.model.StockAnalysisRecord;

@SpringBootTest
@ActiveProfiles("test")
class StockAnalysisMapperTest {

    @Autowired
    private StockAnalysisMapper mapper;

    @Test
    void insertAndFetchRecent() {
        StockAnalysisRecord record = new StockAnalysisRecord();
        record.setStockCode("TEST");
        record.setRawResponse("{\"message\":\"ok\"}");
        record.setRequestedAt(LocalDateTime.now());

        mapper.insert(record);

        assertThat(record.getId()).as("generated id").isNotNull();

        List<StockAnalysisRecord> recent = mapper.findRecent(5);

        assertThat(recent).isNotEmpty();
        assertThat(recent.getFirst().getStockCode()).isEqualTo("TEST");
    }
}
