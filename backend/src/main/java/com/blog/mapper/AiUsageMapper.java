package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.AiUsage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface AiUsageMapper extends BaseMapper<AiUsage> {
    
    @Update("INSERT INTO ai_usage (user_id, date, request_count, total_tokens) " +
            "VALUES (#{userId}, #{date}, 1, #{tokens}) " +
            "ON DUPLICATE KEY UPDATE request_count = request_count + 1, total_tokens = total_tokens + #{tokens}")
    void incrementUsage(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("tokens") Integer tokens);
}
