package com.finance_analysis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.finance_analysis.model.StockAnalysisRecord;

@Mapper
public interface StockAnalysisMapper {

    void insert(StockAnalysisRecord record);

    List<StockAnalysisRecord> findRecent(@Param("limit") int limit);
}
