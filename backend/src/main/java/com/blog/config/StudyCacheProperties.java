package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog.cache.study")
public class StudyCacheProperties {

    /**
     * 学习分类树缓存分钟数
     */
    private long categoryTreeMinutes = 120;

    /**
     * 学习概览缓存分钟数
     */
    private long overviewMinutes = 5;

    /**
     * 抽查统计缓存分钟数
     */
    private long checkStatsMinutes = 5;
}
