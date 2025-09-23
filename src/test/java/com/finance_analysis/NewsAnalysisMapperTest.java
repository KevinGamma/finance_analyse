package com.finance_analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.finance_analysis.mapper.NewsAnalysisMapper;
import com.finance_analysis.model.NewsAnalysisRecord;

@SpringBootTest
@ActiveProfiles("test")
class NewsAnalysisMapperTest {

    @Autowired
    private NewsAnalysisMapper mapper;

    @Test
    void insertAndFetchRecent() {
        NewsAnalysisRecord record = new NewsAnalysisRecord();
        record.setKeyword("AI");
        record.setRawResponse("{\"result\":\"ok\"}");
        record.setRequestedAt(LocalDateTime.now());

        mapper.insert(record);

        assertThat(record.getId()).as("generated id").isNotNull();

        List<NewsAnalysisRecord> recent = mapper.findRecent(5);

        assertThat(recent).isNotEmpty();
        assertThat(recent.getFirst().getKeyword()).isEqualTo("AI");
    }
}
