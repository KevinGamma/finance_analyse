package com.finance_analysis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.finance_analysis.model.NewsAnalysisRecord;

@Mapper
public interface NewsAnalysisMapper {

    void insert(NewsAnalysisRecord record);

    List<NewsAnalysisRecord> findRecent(@Param("limit") int limit);
}
