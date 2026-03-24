package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_config")
public class AiConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String provider;
    
    private String model;
    
    private String displayName;
    
    private String apiKey;
    
    private String baseUrl;
    
    private Integer maxTokens;
    
    private BigDecimal temperature;
    
    private String systemPrompt;
    
    private Integer enabled;
    
    private Integer dailyLimit;
    
    private Integer sortOrder;
    
    private Integer isDefault;

    /**
     * 是否用于AI对话
     */
    private Integer useForChat;

    /**
     * 是否用于学习模块AI评分
     */
    private Integer useForStudyScore;

    /**
     * 是否为学习模块AI评分默认模型
     */
    private Integer isDefaultStudyScore;

    /**
     * 是否支持深度思考（如 DeepSeek R1）
     */
    private Integer supportThinking;

    /**
     * 关联的Logo ID（ai_logo表主键）
     */
    private Long logoId;

    /**
     * 学习模块AI评分系统提示词
     */
    private String scoreSystemPrompt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
