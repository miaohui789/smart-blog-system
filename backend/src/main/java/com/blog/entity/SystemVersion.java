package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("system_version")
public class SystemVersion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String versionNumber;
    
    private String versionName;
    
    private LocalDate releaseDate;
    
    private String description;
    
    private String features;
    
    private String improvements;
    
    private String bugFixes;
    
    private Integer isMajor;
    
    private Integer status;
    
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
