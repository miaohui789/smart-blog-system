package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vip_member")
public class VipMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer level;
    private LocalDateTime startTime;
    private LocalDateTime expireTime;
    private String activationKey;
    private Integer status;
    private Integer heatCountToday;
    private LocalDate heatDate;
    private Integer downloadCountToday;
    private LocalDate downloadDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
}
