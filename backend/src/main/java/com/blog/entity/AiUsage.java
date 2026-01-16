package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("ai_usage")
public class AiUsage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private LocalDate date;
    
    private Integer requestCount;
    
    private Integer totalTokens;
}
