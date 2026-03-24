package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog.study.check")
public class StudyCheckRetentionProperties {

    /**
     * 抽查记录保留天数
     */
    private long historyRetentionDays = 2;

    /**
     * 抽查记录清理 cron
     */
    private String cleanupCron = "0 20 3 * * ?";
}
