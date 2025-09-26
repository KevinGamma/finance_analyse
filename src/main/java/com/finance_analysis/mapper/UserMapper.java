package com.finance_analysis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.finance_analysis.model.User;

@Mapper
public interface UserMapper {

    void insert(User user);

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);
}
