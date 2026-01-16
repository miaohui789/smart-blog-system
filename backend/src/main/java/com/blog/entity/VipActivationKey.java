package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vip_activation_key")
public class VipActivationKey {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyCode;
    private Integer level;
    private Integer durationDays;
    private Long usedBy;
    private LocalDateTime usedTime;
    private Integer status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String usedByUsername;
}
