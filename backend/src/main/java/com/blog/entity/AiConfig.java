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
    
    private String apiKey;
    
    private String baseUrl;
    
    private Integer maxTokens;
    
    private BigDecimal temperature;
    
    private String systemPrompt;
    
    private Integer enabled;
    
    private Integer dailyLimit;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
